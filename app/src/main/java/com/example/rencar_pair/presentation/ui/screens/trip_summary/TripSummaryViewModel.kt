package com.example.rencar_pair.presentation.ui.screens.trip_summary

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class TripSummaryViewModel(
    private val rentalUseCases: RentalUseCases,
    private val paymentUseCases: PaymentUseCases
) : BaseMviViewModel<TripSummaryState, TripSummaryIntent, TripSummaryEffect>(
    TripSummaryState()
) {

    override fun onIntent(intent: TripSummaryIntent) {
        when (intent) {
            is TripSummaryIntent.LoadSummary -> loadSummary(intent.rentalId)
            is TripSummaryIntent.SelectCard -> updateState {
                it.copy(selectedCardToken = intent.token)
            }
            TripSummaryIntent.Pay -> pay()
        }
    }

    private fun loadSummary(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, rentalId = rentalId) }
            when (val result = rentalUseCases.getActiveRental(rentalId)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(rental = result.data) }
                    loadCards()
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadCards() {
        launchCoroutine {
            when (val result = paymentUseCases.getSavedCards()) {
                is NetworkResult.Success -> {
                    val cards = result.data
                    updateState {
                        it.copy(
                            isLoading = false,
                            savedCards = cards,
                            selectedCardToken = cards.firstOrNull()?.cardToken
                        )
                    }
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun pay() {
        val current = currentState()
        val rentalId = current.rentalId
        val cardToken = current.selectedCardToken
        val amount = current.rental?.totalPrice ?: 0.0

        if (cardToken == null) {
            emitEffect(TripSummaryEffect.ShowError("Lütfen bir ödeme yöntemi seçin"))
            return
        }

        launchCoroutine {
            updateState { it.copy(isPaying = true) }
            when (val result = paymentUseCases.processPayment(rentalId, cardToken, amount)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isPaying = false) }
                    emitEffect(TripSummaryEffect.ShowPaymentSuccess("Ödeme başarıyla alındı!"))
                    emitEffect(TripSummaryEffect.NavigateToHome)
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isPaying = false, errorMessage = result.message) }
                    emitEffect(TripSummaryEffect.ShowError(result.message))
                }
            }
        }
    }
}
