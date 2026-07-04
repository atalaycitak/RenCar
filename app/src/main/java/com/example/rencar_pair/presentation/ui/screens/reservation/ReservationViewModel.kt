package com.example.rencar_pair.presentation.ui.screens.reservation

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.CreateRentalUseCase
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class ReservationViewModel(
    private val vehicleId: String,
    private val getVehicleDetailUseCase: GetVehicleDetailUseCase,
    private val calculateReservationQuoteUseCase: CalculateReservationQuoteUseCase,
    private val createRentalUseCase: CreateRentalUseCase
) : BaseMviViewModel<ReservationState, ReservationIntent, ReservationEffect>(
    ReservationState(isLoading = true)
) {

    init {
        onIntent(ReservationIntent.LoadVehicle)
    }

    override fun onIntent(intent: ReservationIntent) {
        when (intent) {
            ReservationIntent.LoadVehicle -> loadVehicle()
            ReservationIntent.IncreaseDays -> updateDays(currentState().selectedDays + 1)
            ReservationIntent.DecreaseDays -> updateDays(currentState().selectedDays - 1)
            ReservationIntent.ConfirmReservation -> confirmReservation()
        }
    }

    private fun loadVehicle() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getVehicleDetailUseCase(vehicleId)) {
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

    private fun updateDays(days: Int) {
        val safeDays = days.coerceIn(1, 30)
        updateState { current ->
            val vehicle = current.vehicle
            current.copy(
                selectedDays = safeDays,
                quote = vehicle?.let { calculateReservationQuoteUseCase(it, safeDays) }
            )
        }
    }

    private fun confirmReservation() {
        val vehicle = currentState().vehicle ?: return
        val quote = currentState().quote
            ?: calculateReservationQuoteUseCase(vehicle, currentState().selectedDays)

        launchCoroutine {
            updateState { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = createRentalUseCase(vehicle.id, quote.endDateIso)) {
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
