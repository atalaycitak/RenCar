package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.distanceTo
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.PaymentStatus
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.VehiclePoint
import com.example.rencar_pair.domain.usecase.ObserveActiveVehicleLocationUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

class ActiveRentalViewModel(
    private val rentalUseCases: RentalUseCases,
    private val vehicleUseCases: VehicleUseCases,
    private val observeActiveVehicleLocationUseCase: ObserveActiveVehicleLocationUseCase
) : BaseMviViewModel<ActiveRentalState, ActiveRentalIntent, ActiveRentalEffect>(
    ActiveRentalState()
) {

    private var timerJob: Job? = null
    private var timerEnabled: Boolean = true
    private var locationTrackingJob: Job? = null
    private var demoLocationJob: Job? = null
    private var hasSocketLocation: Boolean = false

    override fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> loadRental(intent.rentalId)
            ActiveRentalIntent.RequestFinishConfirmation -> updateState {
                it.copy(showFinishConfirmation = true, errorMessage = null)
            }
            ActiveRentalIntent.DismissFinishConfirmation -> updateState {
                it.copy(showFinishConfirmation = false)
            }
            ActiveRentalIntent.FinishRental -> finishRental()
            ActiveRentalIntent.ToggleVehicleLock -> updateState {
                it.copy(isVehicleLocked = !it.isVehicleLocked)
            }
            ActiveRentalIntent.TickTime -> updateSimulation()
        }
    }

    private fun loadRental(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = rentalUseCases.getActiveRental()) {
                is NetworkResult.Success -> {
                    val activeRental = result.data
                    if (activeRental == null) {
                        handleMissingActiveRental(rentalId)
                        return@launchCoroutine
                    }

                    if (activeRental.id != rentalId) {
                        emitEffect(ActiveRentalEffect.ShowError("Açık olan farklı bir aktif kiralama bulundu, güncel yolculuk gösteriliyor."))
                    }

                    updateState {
                        it.copy(
                            isLoading = false,
                            activeRental = activeRental,
                            rental = activeRental.toRental(),
                            elapsedSeconds = activeRental.elapsedSeconds.toLong().coerceAtLeast(0),
                            distanceKm = activeRental.distanceKm ?: 0.0,
                            currentCost = activeRental.currentCost
                        )
                    }
                    loadVehicle(activeRental.vehicleId)
                    startTimer()
                    startLocationTracking()
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadVehicle(vehicleId: String) {
        launchCoroutine {
            when (val result = vehicleUseCases.getVehicleDetail(vehicleId)) {
                is NetworkResult.Success -> updateState { it.copy(vehicle = result.data) }
                is NetworkResult.Error -> {
                    if (currentState().vehicle == null) {
                        emitEffect(ActiveRentalEffect.ShowError(result.message))
                    }
                }
            }
        }
    }

    private fun startLocationTracking() {
        if (locationTrackingJob?.isActive == true) return
        startDemoLocationFallback()
        /*
        // TODO: Sunucudan gelen saçma sapan uzak lokasyonlar simülasyonu bozduğu için tekrar kapatıldı
        locationTrackingJob = launchCoroutine {
            observeActiveVehicleLocationUseCase().collect { point ->
                hasSocketLocation = true
                demoLocationJob?.cancel()
                applyVehiclePoint(point)
            }
        }
        */
    }

    private fun startDemoLocationFallback() {
        if (!timerEnabled) return
        if (demoLocationJob?.isActive == true) return
        demoLocationJob = launchCoroutine {
            delay(DEMO_ROUTE_START_DELAY_MS)
            if (hasSocketLocation || currentState().vehicleLocation != null) return@launchCoroutine

            DEMO_ROUTE_POINTS.forEach { point ->
                if (!isActive || currentState().activeRental == null || hasSocketLocation) {
                    return@launchCoroutine
                }
                applyVehiclePoint(point)
                delay(DEMO_ROUTE_TICK_MS)
            }

            while (isActive && currentState().activeRental != null && !hasSocketLocation) {
                delay(DEMO_ROUTE_TICK_MS)
            }
        }
    }

    private fun applyVehiclePoint(point: VehiclePoint) {
        val current = currentState()
        val lastPoint = current.vehicleLocation
        val addedDistance = if (lastPoint != null) {
            lastPoint.distanceTo(point)
        } else {
            0.0
        }

        updateState {
            it.copy(
                isGpsConnected = true,
                vehicleLocation = point,
                routePoints = (it.routePoints + point).takeLast(MAX_ROUTE_POINTS),
                distanceKm = it.distanceKm + addedDistance
            )
        }
    }

    private fun startTimer() {
        if (!timerEnabled) return
        timerJob?.cancel()
        timerJob = launchCoroutine {
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                onIntent(ActiveRentalIntent.TickTime)
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        locationTrackingJob?.cancel()
        demoLocationJob?.cancel()
        super.onCleared()
    }

    private fun finishRental() {
        val rentalId = currentState().activeRental?.id ?: currentState().rental?.id ?: return
        launchCoroutine {
            updateState {
                it.copy(
                    isFinishing = true,
                    showFinishConfirmation = false,
                    errorMessage = null
                )
            }

            val result = when (val finishResult = rentalUseCases.finishRental(rentalId)) {
                is NetworkResult.Success -> NetworkResult.Success(finishResult.data.id)
                is NetworkResult.Error -> {
                    if (finishResult.shouldTryReturnEndpoint()) {
                        when (val returnResult = rentalUseCases.returnRental(rentalId)) {
                            is NetworkResult.Success -> NetworkResult.Success(returnResult.data.id)
                            is NetworkResult.Error -> NetworkResult.Error(returnResult.message, returnResult.code)
                        }
                    } else {
                        when (val stateResult = rentalUseCases.getRental(rentalId)) {
                            is NetworkResult.Success -> {
                                if (stateResult.data.status == RentalStatus.Completed) {
                                    NetworkResult.Success(stateResult.data.id)
                                } else {
                                    NetworkResult.Error(finishResult.message, finishResult.code)
                                }
                            }
                            is NetworkResult.Error -> NetworkResult.Error(finishResult.message, finishResult.code)
                        }
                    }
                }
            }

            when (result) {
                is NetworkResult.Success -> {
                    timerJob?.cancel()
                    locationTrackingJob?.cancel()
                    demoLocationJob?.cancel()
                    updateState {
                        it.copy(
                            isFinishing = false,
                            activeRental = null,
                            rental = it.rental?.copy(id = result.data)
                        )
                    }
                    emitEffect(ActiveRentalEffect.NavigateToSummary(result.data))
                }
                is NetworkResult.Error -> {
                    updateState {
                        it.copy(
                            isFinishing = false,
                            errorMessage = result.message
                        )
                    }
                    emitEffect(ActiveRentalEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun updateSimulation() {
        val current = currentState()
        if (current.activeRental != null && !current.isFinishing) {
            updateState {
                it.copy(
                    elapsedSeconds = current.elapsedSeconds + 1,
                    currentCost = current.currentCost + COST_PER_SECOND
                )
            }
        }
    }

    internal fun setTimerEnabled(enabled: Boolean) {
        timerEnabled = enabled
        if (!enabled) {
            timerJob?.cancel()
            timerJob = null
            demoLocationJob?.cancel()
            demoLocationJob = null
        }
    }

    private suspend fun clearActiveRental(message: String) {
        timerJob?.cancel()
        locationTrackingJob?.cancel()
        demoLocationJob?.cancel()
        updateState {
            it.copy(
                isLoading = false,
                activeRental = null,
                rental = null,
                errorMessage = message
            )
        }
        emitEffect(ActiveRentalEffect.ShowError(message))
        emitEffect(ActiveRentalEffect.NavigateToHome)
    }

    private suspend fun handleMissingActiveRental(rentalId: String) {
        when (val rentalResult = rentalUseCases.getRental(rentalId)) {
            is NetworkResult.Success -> {
                if (rentalResult.data.status == RentalStatus.Completed) {
                    timerJob?.cancel()
                    locationTrackingJob?.cancel()
                    demoLocationJob?.cancel()
                    updateState {
                        it.copy(
                            isLoading = false,
                            activeRental = null,
                            rental = rentalResult.data,
                            errorMessage = null
                        )
                    }
                    emitEffect(ActiveRentalEffect.NavigateToSummary(rentalResult.data.id))
                } else {
                    clearActiveRental("Aktif kiralama bulunamadı.")
                }
            }
            is NetworkResult.Error -> clearActiveRental("Aktif kiralama bulunamadı.")
        }
    }

    private fun ActiveRental.toRental(): Rental {
        return Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            plan = plan,
            status = status,
            paymentStatus = PaymentStatus.Unpaid,
            paymentMethod = null,
            totalPrice = currentCost,
            startFee = startFee,
            serviceFee = null,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            discountAmount = null,
            startedAt = startedAt,
            endedAt = null,
            scheduledEndDate = null,
            createdAt = createdAt
        )
    }

    private companion object {
        const val TICK_INTERVAL_MS = 1_000L
        const val DEMO_ROUTE_START_DELAY_MS = 2_000L
        const val DEMO_ROUTE_TICK_MS = 1_000L
        const val MAX_ROUTE_POINTS = 240
        const val COST_PER_MINUTE = 2.5
        const val COST_PER_SECOND = COST_PER_MINUTE / 60.0

        val DEMO_ROUTE_POINTS = buildDemoRoutePoints()

        private fun buildDemoRoutePoints(): List<VehiclePoint> {
            val startLat = 41.03720
            val startLng = 28.98590
            val points = mutableListOf(VehiclePoint(startLat, startLng))
            val random = java.util.Random()
            
            var currentLat = startLat
            var currentLng = startLng
            var currentDirection = 0 // 0: Kuzey, 1: Doğu, 2: Güney, 3: Batı

            for (i in 1..240) {
                // Her saniye yön değiştir (Aşırı Zikzak)
                val turn = if (random.nextBoolean()) 1 else -1 // Sağa veya Sola 90 derece dön
                currentDirection = (currentDirection + turn + 4) % 4

                // Seçili yönde ilerle
                val stepSize = 0.0002
                when (currentDirection) {
                    0 -> currentLat += stepSize
                    1 -> currentLng += stepSize
                    2 -> currentLat -= stepSize
                    3 -> currentLng -= stepSize
                }
                
                // Çok hafif bir titreme (gerçekçi GPS hissi için)
                val noiseLat = (random.nextDouble() - 0.5) * 0.00004
                val noiseLng = (random.nextDouble() - 0.5) * 0.00004
                
                points.add(VehiclePoint(currentLat + noiseLat, currentLng + noiseLng))
            }
            return points
        }
    }

    private fun NetworkResult.Error.shouldTryReturnEndpoint(): Boolean {
        val normalized = message.lowercase()
        val mentionsLegacyReturn =
            "daily" in normalized ||
            "günlük" in normalized ||
            "iade" in normalized ||
            "return" in normalized
        return mentionsLegacyReturn && code in setOf(400, 409, 422)
    }
}
