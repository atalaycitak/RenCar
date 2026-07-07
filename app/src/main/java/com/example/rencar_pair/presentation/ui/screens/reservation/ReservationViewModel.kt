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


    private fun confirmReservation() {
        val vehicle = currentState().vehicle ?: return
        val quote = currentState().quote
            ?: calculateReservationQuoteUseCase(vehicle, currentState().selectedDays)

        launchCoroutine {
            updateState { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = rentalUseCases.createRental(vehicle.id, quote.endDateIso)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(isSubmitting = false, rentalId = result.data.id)
                    }
                    emitEffect(ReservationEffect.NavigateToDelivery(result.data.id, vehicle.id))
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
