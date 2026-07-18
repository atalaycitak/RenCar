package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit

class ReservationViewModel(
    savedStateHandle: SavedStateHandle,
    private val vehicleUseCases: VehicleUseCases,
    private val calculateReservationQuoteUseCase: CalculateReservationQuoteUseCase,
    private val rentalUseCases: RentalUseCases
) : BaseMviViewModel<ReservationState, ReservationIntent, ReservationEffect>(
    ReservationState(isLoading = true)
) {

    private val vehicleId: String = savedStateHandle.get<String>("vehicleId")!!

    init {
        onIntent(ReservationIntent.LoadVehicle)
        loadActiveReservation()
    }

    override fun onIntent(intent: ReservationIntent) {
        when (intent) {
            ReservationIntent.LoadVehicle -> loadVehicle()
            ReservationIntent.IncreaseDays -> updateState { current ->
                val newDays = (current.selectedDays + 1).coerceIn(1, 30)
                current.copy(
                    selectedDays = newDays,
                    quote = current.vehicle?.let { calculateReservationQuoteUseCase(it, newDays) }
                )
            }
            ReservationIntent.DecreaseDays -> updateState { current ->
                val newDays = (current.selectedDays - 1).coerceIn(1, 30)
                current.copy(
                    selectedDays = newDays,
                    quote = current.vehicle?.let { calculateReservationQuoteUseCase(it, newDays) }
                )
            }
            is ReservationIntent.SelectDays -> updateState { current ->
                val newDays = intent.days.coerceIn(1, 30)
                current.copy(
                    selectedDays = newDays,
                    quote = current.vehicle?.let { calculateReservationQuoteUseCase(it, newDays) },
                    errorMessage = null
                )
            }
            is ReservationIntent.SelectPlan -> updateState { current ->
                current.copy(selectedPlan = intent.plan, errorMessage = null)
            }
            ReservationIntent.ToggleTermsAccepted -> updateState { current ->
                current.copy(termsAccepted = !current.termsAccepted, errorMessage = null)
            }
            ReservationIntent.ConfirmReservation -> confirmReservation()
        }
    }

    private fun loadVehicle() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getVehicleDetail(vehicleId)) {
                is NetworkResult.Success -> updateState {
                    it.copy(
                        vehicle = result.data,
                        quote = calculateReservationQuoteUseCase(result.data, it.selectedDays),
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> updateState {
                    val reservedVehicle = it.activeReservation
                        ?.takeIf { reservation -> reservation.vehicleId == vehicleId }
                        ?.vehicle
                    if (reservedVehicle != null) {
                        it.copy(
                            vehicle = reservedVehicle,
                            quote = calculateReservationQuoteUseCase(reservedVehicle, it.selectedDays),
                            isLoading = false,
                            errorMessage = null
                        )
                    } else {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun loadActiveReservation() {
        launchCoroutine {
            when (val result = rentalUseCases.getActiveReservation()) {
                is NetworkResult.Success -> updateState { state ->
                    val reservedVehicle = result.data
                        ?.takeIf { reservation -> reservation.vehicleId == vehicleId }
                        ?.vehicle
                    state.copy(
                        activeReservation = result.data,
                        vehicle = reservedVehicle ?: state.vehicle,
                        quote = reservedVehicle?.let { calculateReservationQuoteUseCase(it, state.selectedDays) }
                            ?: state.quote,
                        isLoading = if (reservedVehicle != null) false else state.isLoading,
                        errorMessage = if (reservedVehicle != null) null else state.errorMessage
                    )
                }
                is NetworkResult.Error -> updateState { state ->
                    state.copy(errorMessage = result.message)
                }
            }
        }
    }

    private fun confirmReservation() {
        val vehicle = currentState().vehicle ?: return
        if (!currentState().termsAccepted) {
            updateState {
                it.copy(errorMessage = "Rezervasyonu tamamlamak için kullanım şartlarını onaylamalısın.")
            }
            return
        }
        val activeReservation = currentState().activeReservation
        if (activeReservation?.vehicleId == vehicle.id) {
            unlockReservedVehicle(vehicle.id)
            return
        }

        launchCoroutine {
            updateState { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = rentalUseCases.createReservation(vehicle.id)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(
                            activeReservation = result.data,
                            vehicle = vehicle.copy(
                                status = com.example.rencar_pair.domain.model.VehicleStatus.Reserved,
                                canReserve = false,
                                canUnlock = true
                            )
                        )
                    }
                    unlockReservedVehicle(vehicle.id)
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun unlockReservedVehicle(vehicleId: String) {
        launchCoroutine {
            updateState { it.copy(isSubmitting = true, errorMessage = null) }
            val state = currentState()
            val plan = state.selectedPlan
            val endDate = if (plan == RentalPlan.Daily) {
                Instant.now().plus(state.selectedDays.toLong(), ChronoUnit.DAYS).toString()
            } else {
                null
            }
            when (val result = rentalUseCases.createRental(vehicleId = vehicleId, plan = plan, endDate = endDate)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(isSubmitting = false, rentalId = result.data.id)
                    }
                    if (result.data.status == RentalStatus.Active || plan == RentalPlan.Daily) {
                        emitEffect(ReservationEffect.NavigateToActiveRental(result.data.id))
                    } else {
                        emitEffect(ReservationEffect.NavigateToDelivery(result.data.id, vehicleId))
                    }
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
