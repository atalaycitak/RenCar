package com.example.rencar_pair.presentation.ui.screens.trip_summary

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.PaymentRepository
import com.example.rencar_pair.domain.usecase.payment.ProcessPaymentUseCase
import com.example.rencar_pair.domain.usecase.rental.GetActiveRentalUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class TripSummaryViewModel(
    private val getActiveRentalUseCase: GetActiveRentalUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val paymentRepository: PaymentRepository
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
            when (val result = getActiveRentalUseCase(rentalId)) {
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
            when (val result = paymentRepository.getSavedCards()) {
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
            when (val result = processPaymentUseCase(rentalId, cardToken, amount)) {
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
