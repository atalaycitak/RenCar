package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

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
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun loadActiveReservation() {
        launchCoroutine {
            when (val result = rentalUseCases.getActiveReservation()) {
                is NetworkResult.Success -> updateState { state ->
                    state.copy(activeReservation = result.data)
                }
                is NetworkResult.Error -> updateState { state ->
                    state.copy(errorMessage = result.message)
                }
            }
        }
    }

    private fun confirmReservation() {
        val vehicle = currentState().vehicle ?: return
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
                            isSubmitting = false,
                            activeReservation = result.data,
                            vehicle = vehicle.copy(
                                status = com.example.rencar_pair.domain.model.VehicleStatus.Reserved,
                                canReserve = false,
                                canUnlock = true
                            )
                        )
                    }
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
            when (val result = rentalUseCases.createRental(vehicleId = vehicleId, plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(isSubmitting = false, rentalId = result.data.id)
                    }
                    emitEffect(ReservationEffect.NavigateToDelivery(result.data.id, vehicleId))
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
