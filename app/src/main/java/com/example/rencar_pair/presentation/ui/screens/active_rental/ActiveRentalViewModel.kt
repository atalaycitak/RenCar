package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.distanceTo
import com.example.rencar_pair.domain.usecase.ObserveActiveVehicleLocationUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.Instant

class ActiveRentalViewModel(
    private val rentalUseCases: RentalUseCases,
    private val vehicleUseCases: VehicleUseCases,
    private val observeActiveVehicleLocationUseCase: ObserveActiveVehicleLocationUseCase
) : BaseMviViewModel<ActiveRentalState, ActiveRentalIntent, ActiveRentalEffect>(
    ActiveRentalState()
) {

    private var timerJob: Job? = null
    private var timerEnabled: Boolean = true

    override fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> loadRental(intent.rentalId)
            ActiveRentalIntent.FinishRental -> finishRental()
            ActiveRentalIntent.TickTime -> updateSimulation()
        }
    }

    private fun loadRental(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = rentalUseCases.getRental(rentalId)) {
                is NetworkResult.Success -> {
                    val startedTime = result.data.startedAt ?: result.data.createdAt
                    val elapsedMinutes = startedTime.elapsedMinutesUntilNow()
                    updateState {
                        it.copy(
                            isLoading = false,
                            rental = result.data,
                            vehicle = result.data.vehicle,
                            elapsedMinutes = elapsedMinutes,
                            distanceKm = 0.0,
                            currentCost = result.data.estimatedCurrentCost(elapsedMinutes)
                        )
                    }
                    loadVehicle(result.data.vehicleId)
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
        launchCoroutine {
            observeActiveVehicleLocationUseCase().collect { point ->
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
                        routePoints = it.routePoints + point,
                        distanceKm = it.distanceKm + addedDistance
                    )
                }
            }
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
        super.onCleared()
    }

    private fun finishRental() {
        val rentalId = currentState().rental?.id ?: return
        emitEffect(ActiveRentalEffect.NavigateToReturnVehicle(rentalId))
    }

    private fun updateSimulation() {
        val current = currentState()
        if (current.rental != null && !current.isFinishing) {
            updateState {
                it.copy(
                    elapsedMinutes = current.elapsedMinutes + 1,
                    currentCost = current.currentCost + COST_PER_MINUTE
                )
            }
        }
    }

    internal fun setTimerEnabled(enabled: Boolean) {
        timerEnabled = enabled
        if (!enabled) {
            timerJob?.cancel()
            timerJob = null
        }
    }

    private fun Instant.elapsedMinutesUntilNow(): Int {
        return Duration.between(this, Instant.now()).toMinutes().coerceAtLeast(0).toInt()
    }

    private fun com.example.rencar_pair.domain.model.Rental.estimatedCurrentCost(elapsedMinutes: Int): Double {
        return (totalPrice ?: 0.0) + elapsedMinutes * COST_PER_MINUTE
    }

    private companion object {
        const val TICK_INTERVAL_MS = 60_000L
        const val COST_PER_MINUTE = 2.5
    }
}
