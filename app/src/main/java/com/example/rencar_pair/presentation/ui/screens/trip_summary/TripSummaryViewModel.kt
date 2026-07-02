package com.example.rencar_pair.presentation.ui.screens.trip_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.usecase.payment.ProcessPaymentUseCase
import com.example.rencar_pair.domain.usecase.rental.GetActiveRentalUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripSummaryViewModel(
    private val getActiveRentalUseCase: GetActiveRentalUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TripSummaryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<TripSummaryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: TripSummaryIntent) {
        when (intent) {
            is TripSummaryIntent.LoadSummary -> loadSummary(intent.rentalId)
            is TripSummaryIntent.SelectCard -> _state.update { it.copy(selectedCardToken = intent.token) }
            TripSummaryIntent.Pay -> pay()
        }
    }

    private fun loadSummary(rentalId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, rentalId = rentalId) }
            when (val result = getActiveRentalUseCase(rentalId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(rental = result.data) }
                    loadCards()
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun loadCards() {
        viewModelScope.launch {
            when (val result = paymentRepository.getSavedCards()) {
                is NetworkResult.Success -> {
                    val cards = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            savedCards = cards,
                            selectedCardToken = cards.firstOrNull()?.cardToken
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun pay() {
        val current = state.value
        val rentalId = current.rentalId
        val cardToken = current.selectedCardToken
        val amount = current.rental?.totalPrice ?: 0.0

        if (cardToken == null) {
            viewModelScope.launch { _effect.send(TripSummaryEffect.ShowError("Lütfen bir ödeme yöntemi seçin")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isPaying = true) }
            when (val result = processPaymentUseCase(rentalId, cardToken, amount)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isPaying = false) }
                    _effect.send(TripSummaryEffect.ShowPaymentSuccess("Ödeme başarıyla alındı!"))
                    _effect.send(TripSummaryEffect.NavigateToHome)
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isPaying = false, errorMessage = result.message) }
                    _effect.send(TripSummaryEffect.ShowError(result.message ?: "Ödeme başarısız oldu"))
                }
                NetworkResult.Loading -> Unit
            }
        }
    }
}
