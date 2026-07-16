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
            TripSummaryIntent.ShowAddCardDialog -> updateState {
                it.copy(
                    isAddCardDialogVisible = true,
                    cardFormError = null
                )
            }
            TripSummaryIntent.HideAddCardDialog -> updateState {
                it.copy(
                    isAddCardDialogVisible = false,
                    isSavingCard = false,
                    cardHolderName = "",
                    cardNumber = "",
                    cardExpiry = "",
                    cardCvc = "",
                    cardFormError = null
                )
            }
            is TripSummaryIntent.UpdateCardHolderName -> updateState {
                it.copy(cardHolderName = intent.value.take(40), cardFormError = null)
            }
            is TripSummaryIntent.UpdateCardNumber -> updateState {
                it.copy(cardNumber = intent.value.onlyDigits().take(16), cardFormError = null)
            }
            is TripSummaryIntent.UpdateCardExpiry -> updateState {
                it.copy(cardExpiry = intent.value.formatExpiry(), cardFormError = null)
            }
            is TripSummaryIntent.UpdateCardCvc -> updateState {
                it.copy(cardCvc = intent.value.onlyDigits().take(4), cardFormError = null)
            }
            TripSummaryIntent.SubmitCard -> submitCard()
            TripSummaryIntent.Pay -> pay()
        }
    }

    private fun loadSummary(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, rentalId = rentalId) }
            when (val result = rentalUseCases.getRental(rentalId)) {
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

    private fun submitCard() {
        val current = currentState()
        val cardNumber = current.cardNumber.onlyDigits()
        val expiryParts = current.cardExpiry.split("/")
        val expireMonth = expiryParts.getOrNull(0).orEmpty()
        val expireYear = expiryParts.getOrNull(1).orEmpty()

        val validationError = when {
            current.cardHolderName.isBlank() -> "Kart üzerindeki isim gerekli"
            cardNumber.length != 16 -> "Kart numarası 16 haneli olmalı"
            expiryParts.size != 2 || expireMonth.length != 2 || expireYear.length != 2 -> "Son kullanma tarihi AA/YY formatında olmalı"
            expireMonth.toIntOrNull() !in 1..12 -> "Son kullanma ayı geçersiz"
            current.cardCvc.length < 3 -> "CVC en az 3 haneli olmalı"
            else -> null
        }

        if (validationError != null) {
            updateState { it.copy(cardFormError = validationError) }
            return
        }

        launchCoroutine {
            updateState { it.copy(isSavingCard = true, cardFormError = null) }
            when (
                val result = paymentUseCases.addCard(
                    cardNumber = cardNumber,
                    expireMonth = expireMonth,
                    expireYear = "20$expireYear",
                    cvc = current.cardCvc,
                    cardHolderName = current.cardHolderName.trim()
                )
            ) {
                is NetworkResult.Success -> {
                    val newCard = result.data
                    updateState {
                        it.copy(
                            savedCards = it.savedCards + newCard,
                            selectedCardToken = newCard.cardToken,
                            isAddCardDialogVisible = false,
                            isSavingCard = false,
                            cardHolderName = "",
                            cardNumber = "",
                            cardExpiry = "",
                            cardCvc = "",
                            cardFormError = null
                        )
                    }
                    emitEffect(TripSummaryEffect.ShowPaymentSuccess("Kart kaydedildi ve ödeme için seçildi"))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isSavingCard = false, cardFormError = result.message) }
                    emitEffect(TripSummaryEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun String.onlyDigits(): String = filter { it.isDigit() }

    private fun String.formatExpiry(): String {
        val digits = onlyDigits().take(4)
        return if (digits.length <= 2) {
            digits
        } else {
            "${digits.take(2)}/${digits.drop(2)}"
        }
    }
}
