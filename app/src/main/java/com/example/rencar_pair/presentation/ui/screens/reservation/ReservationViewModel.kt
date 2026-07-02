package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.CreateRentalUseCase
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val vehicleId: String,
    private val getVehicleDetailUseCase: GetVehicleDetailUseCase,
    private val calculateReservationQuoteUseCase: CalculateReservationQuoteUseCase,
    private val createRentalUseCase: CreateRentalUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReservationState(isLoading = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<ReservationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(ReservationIntent.LoadVehicle)
    }

    fun onIntent(intent: ReservationIntent) {
        when (intent) {
            ReservationIntent.LoadVehicle -> loadVehicle()
            ReservationIntent.IncreaseDays -> updateDays(_state.value.selectedDays + 1)
            ReservationIntent.DecreaseDays -> updateDays(_state.value.selectedDays - 1)
            ReservationIntent.ConfirmReservation -> confirmReservation()
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getVehicleDetailUseCase(vehicleId)) {
                is NetworkResult.Success -> _state.update {
                    it.copy(
                        vehicle = result.data,
                        quote = calculateReservationQuoteUseCase(result.data, it.selectedDays),
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun updateDays(days: Int) {
        val safeDays = days.coerceIn(1, 30)
        _state.update { current ->
            val vehicle = current.vehicle
            current.copy(
                selectedDays = safeDays,
                quote = vehicle?.let { calculateReservationQuoteUseCase(it, safeDays) }
            )
        }
    }

    private fun confirmReservation() {
        val vehicle = _state.value.vehicle ?: return
        val quote = _state.value.quote ?: calculateReservationQuoteUseCase(vehicle, _state.value.selectedDays)

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = createRentalUseCase(vehicle.id, quote.endDateIso)) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            rentalId = result.data.id
                        )
                    }
                    _effect.send(ReservationEffect.NavigateToDelivery(result.data.id, vehicle.id))
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }
}
