package com.example.rencar_pair.presentation.ui.screens.trip_summary

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import java.util.Locale

class TripSummaryViewModel(
    private val rentalUseCases: RentalUseCases,
    private val paymentUseCases: PaymentUseCases
) : BaseMviViewModel<TripSummaryState, TripSummaryIntent, TripSummaryEffect>(
    TripSummaryState()
) {

    override fun onIntent(intent: TripSummaryIntent) {
        when (intent) {
            is TripSummaryIntent.LoadSummary -> loadSummary(intent.rentalId)
            is TripSummaryIntent.SelectPaymentMethod -> selectPaymentMethod(intent.method)
            is TripSummaryIntent.SelectCard -> updateState {
                it.copy(selectedCardToken = intent.token, selectedPaymentMethod = PaymentMethod.Card)
            }
            TripSummaryIntent.ShowAddCardDialog -> updateState {
                it.copy(isAddCardDialogVisible = true, cardFormError = null)
            }
            TripSummaryIntent.HideAddCardDialog -> updateState { it.resetCardForm(isVisible = false) }
            TripSummaryIntent.ShowTopUpDialog -> showTopUpDialog()
            TripSummaryIntent.HideTopUpDialog -> updateState {
                it.copy(isTopUpDialogVisible = false, isToppingUp = false, topUpAmount = "")
            }
            is TripSummaryIntent.UpdateTopUpAmount -> updateState {
                it.copy(topUpAmount = intent.amount.asAmountInput(), errorMessage = null)
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
            TripSummaryIntent.SubmitTopUp -> submitTopUp()
            TripSummaryIntent.Pay -> pay()
        }
    }

    private fun loadSummary(rentalId: String) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, rentalId = rentalId, errorMessage = null) }
            when (val rentalResult = rentalUseCases.getRental(rentalId)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(rental = rentalResult.data) }
                    loadPaymentState()
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = rentalResult.message) }
                    emitEffect(TripSummaryEffect.ShowError(rentalResult.message))
                }
            }
        }
    }

    private suspend fun loadPaymentState() {
        val walletInfo = when (val walletResult = paymentUseCases.getWalletInfo()) {
            is NetworkResult.Success -> walletResult.data
            is NetworkResult.Error -> {
                emitEffect(TripSummaryEffect.ShowError(walletResult.message))
                null
            }
        }
        val cards = when (val cardsResult = paymentUseCases.getSavedCards()) {
            is NetworkResult.Success -> cardsResult.data
            is NetworkResult.Error -> {
                emitEffect(TripSummaryEffect.ShowError(cardsResult.message))
                emptyList()
            }
        }

        updateState {
            val selectedCard = cards.firstOrNull { card -> card.cardToken == it.selectedCardToken }
                ?: cards.firstOrNull { card -> card.isDefault }
                ?: cards.firstOrNull()
            val shortfall = ((it.rental?.totalPrice ?: 0.0) - (walletInfo?.balance ?: 0.0))
                .coerceAtLeast(0.0)
            val method = resolveInitialMethod(
                totalPrice = it.rental?.totalPrice ?: 0.0,
                walletInfo = walletInfo,
                cards = cards
            )
            it.copy(
                isLoading = false,
                walletInfo = walletInfo,
                savedCards = cards,
                selectedCardToken = selectedCard?.cardToken,
                selectedPaymentMethod = method,
                topUpAmount = shortfall.formatAmountForInput()
            )
        }
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        val current = currentState()
        if (method == PaymentMethod.Card && current.savedCards.isEmpty()) {
            updateState {
                it.copy(selectedPaymentMethod = PaymentMethod.Card, isAddCardDialogVisible = true)
            }
            emitEffect(TripSummaryEffect.ShowError("Kartla odemek icin once kart ekleyin."))
            return
        }
        updateState { it.copy(selectedPaymentMethod = method, errorMessage = null) }
    }

    private fun pay() {
        val current = currentState()
        val rentalId = current.rentalId
        val amount = current.totalPrice

        if (current.rental == null) {
            emitEffect(TripSummaryEffect.ShowError("Kiralama bilgisi bulunamadi."))
            return
        }

        when (current.selectedPaymentMethod) {
            PaymentMethod.Wallet -> {
                if (current.walletBalance < amount) {
                    if (current.defaultCard == null) {
                        updateState { it.copy(isAddCardDialogVisible = true) }
                        emitEffect(TripSummaryEffect.ShowError("Odeme veya bakiye yukleme icin once kart ekleyin."))
                        return
                    }
                    updateState {
                        it.copy(
                            isTopUpDialogVisible = true,
                            topUpAmount = it.walletShortfall.formatAmountForInput()
                        )
                    }
                    emitEffect(TripSummaryEffect.ShowError("Bakiye yetersiz. Kartla odeyebilir veya bakiye yukleyebilirsiniz."))
                    return
                }
                submitPayment(rentalId, PaymentMethod.Wallet)
            }
            PaymentMethod.Card -> {
                val cardId = current.selectedCard?.cardToken
                if (cardId == null) {
                    updateState { it.copy(isAddCardDialogVisible = true) }
                    emitEffect(TripSummaryEffect.ShowError("Odeme icin bir kart ekleyin."))
                    return
                }
                submitPayment(rentalId, PaymentMethod.Card, cardId = cardId)
            }
            PaymentMethod.Iyzico -> {
                emitEffect(TripSummaryEffect.ShowError("Iyzico akisi bu ekranda izole tutuluyor."))
            }
        }
    }

    private fun submitPayment(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null
    ) {
        launchCoroutine {
            updateState { it.copy(isPaying = true, errorMessage = null) }
            when (
                val result = paymentUseCases.payRental(
                    rentalId = rentalId,
                    method = method,
                    cardId = cardId
                )
            ) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(
                            isPaying = false,
                            paymentResult = result.data,
                            walletInfo = result.data.walletBalance?.let { balance ->
                                it.walletInfo?.copy(balance = balance)
                            } ?: it.walletInfo
                        )
                    }
                    emitEffect(TripSummaryEffect.ShowPaymentSuccess("Odeme basariyla alindi."))
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
            current.cardHolderName.isBlank() -> "Kart uzerindeki isim gerekli"
            cardNumber.length != 16 -> "Kart numarasi 16 haneli olmali"
            expiryParts.size != 2 || expireMonth.length != 2 || expireYear.length != 2 -> "Son kullanma tarihi AA/YY formatinda olmali"
            expireMonth.toIntOrNull() !in 1..12 -> "Son kullanma ayi gecersiz"
            current.cardCvc.length < 3 -> "CVC en az 3 haneli olmali"
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
                        it.resetCardForm(isVisible = false).copy(
                            savedCards = it.savedCards + newCard,
                            selectedCardToken = newCard.cardToken,
                            selectedPaymentMethod = PaymentMethod.Card
                        )
                    }
                    emitEffect(TripSummaryEffect.ShowPaymentSuccess("Kart kaydedildi ve odeme icin secildi."))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isSavingCard = false, cardFormError = result.message) }
                    emitEffect(TripSummaryEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun showTopUpDialog() {
        val current = currentState()
        if (current.defaultCard == null) {
            updateState { it.copy(isAddCardDialogVisible = true) }
            emitEffect(TripSummaryEffect.ShowError("Bakiye yuklemek icin once kart ekleyin."))
            return
        }
        updateState {
            it.copy(
                isTopUpDialogVisible = true,
                topUpAmount = it.walletShortfall.takeIf { shortfall -> shortfall > 0.0 }
                    ?.formatAmountForInput()
                    .orEmpty()
            )
        }
    }

    private fun submitTopUp() {
        val current = currentState()
        val amount = current.topUpAmount.toAmountOrNull()
        val card = current.defaultCard
        if (amount == null || amount <= 0) {
            emitEffect(TripSummaryEffect.ShowError("Gecerli bir tutar girin."))
            return
        }
        if (card == null) {
            updateState { it.copy(isAddCardDialogVisible = true) }
            emitEffect(TripSummaryEffect.ShowError("Bakiye yuklemek icin once kart ekleyin."))
            return
        }

        launchCoroutine {
            updateState { it.copy(isToppingUp = true, errorMessage = null) }
            when (val result = paymentUseCases.topUpWallet(amount, card.cardToken)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(
                            isToppingUp = false,
                            isTopUpDialogVisible = false,
                            walletInfo = result.data,
                            selectedPaymentMethod = PaymentMethod.Wallet,
                            topUpAmount = ""
                        )
                    }
                    emitEffect(TripSummaryEffect.ShowPaymentSuccess("Bakiye yuklendi."))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isToppingUp = false, errorMessage = result.message) }
                    emitEffect(TripSummaryEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun resolveInitialMethod(
        totalPrice: Double,
        walletInfo: WalletInfo?,
        cards: List<SavedCard>
    ): PaymentMethod {
        return when {
            (walletInfo?.balance ?: 0.0) >= totalPrice -> PaymentMethod.Wallet
            cards.isNotEmpty() -> PaymentMethod.Card
            else -> PaymentMethod.Wallet
        }
    }

    private fun TripSummaryState.resetCardForm(isVisible: Boolean): TripSummaryState {
        return copy(
            isAddCardDialogVisible = isVisible,
            isSavingCard = false,
            cardHolderName = "",
            cardNumber = "",
            cardExpiry = "",
            cardCvc = "",
            cardFormError = null
        )
    }

    private fun String.onlyDigits(): String = filter { it.isDigit() }

    private fun String.formatExpiry(): String {
        val digits = onlyDigits().take(4)
        return if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
    }

    private fun String.asAmountInput(): String {
        return filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
            .take(9)
    }

    private fun String.toAmountOrNull(): Double? = replace(',', '.').toDoubleOrNull()

    private fun Double.formatAmountForInput(): String {
        return if (this <= 0.0) "" else String.format(Locale.US, "%.2f", this)
    }
}
