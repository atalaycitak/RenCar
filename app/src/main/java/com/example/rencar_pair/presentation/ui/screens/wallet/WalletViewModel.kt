package com.example.rencar_pair.presentation.ui.screens.wallet

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class WalletViewModel(
    private val paymentUseCases: PaymentUseCases
) : BaseMviViewModel<WalletState, WalletIntent, WalletEffect>(WalletState()) {

    init {
        onIntent(WalletIntent.LoadWallet)
    }

    override fun onIntent(intent: WalletIntent) {
        when (intent) {
            WalletIntent.LoadWallet -> loadWallet()
            is WalletIntent.SelectCard -> setDefaultCard(intent.token)
            WalletIntent.ShowTopUpDialog -> showTopUpDialog()
            WalletIntent.HideTopUpDialog -> updateState {
                it.copy(isTopUpDialogVisible = false, isToppingUp = false, topUpAmount = "")
            }
            WalletIntent.ShowAddCardDialog -> updateState {
                it.copy(isAddCardDialogVisible = true, cardFormError = null)
            }
            WalletIntent.HideAddCardDialog -> updateState { it.resetCardForm(isVisible = false) }
            is WalletIntent.UpdateTopUpAmount -> updateState {
                it.copy(topUpAmount = intent.amount.asAmountInput())
            }
            is WalletIntent.UpdateCardHolderName -> updateState {
                it.copy(cardHolderName = intent.value.take(40), cardFormError = null)
            }
            is WalletIntent.UpdateCardNumber -> updateState {
                it.copy(cardNumber = intent.value.onlyDigits().take(16), cardFormError = null)
            }
            is WalletIntent.UpdateCardExpiry -> updateState {
                it.copy(cardExpiry = intent.value.formatExpiry(), cardFormError = null)
            }
            is WalletIntent.UpdateCardCvc -> updateState {
                it.copy(cardCvc = intent.value.onlyDigits().take(4), cardFormError = null)
            }
            WalletIntent.SubmitCard -> submitCard()
            WalletIntent.SubmitTopUp -> submitTopUp()
        }
    }

    private fun loadWallet() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            val walletInfo = when (val result = paymentUseCases.getWalletInfo()) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> {
                    emitEffect(WalletEffect.ShowError(result.message))
                    null
                }
            }
            val cards = when (val result = paymentUseCases.getSavedCards()) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> {
                    emitEffect(WalletEffect.ShowError(result.message))
                    emptyList()
                }
            }
            updateState {
                val selected = cards.firstOrNull { card -> card.cardToken == it.selectedCardToken }
                    ?: cards.firstOrNull { card -> card.isDefault }
                    ?: cards.firstOrNull()
                it.copy(
                    isLoading = false,
                    walletInfo = walletInfo,
                    savedCards = cards,
                    selectedCardToken = selected?.cardToken
                )
            }
        }
    }

    private fun showTopUpDialog() {
        updateState { it.copy(isTopUpDialogVisible = true, topUpAmount = "") }
    }

    private fun submitTopUp() {
        val current = currentState()
        val amount = current.topUpAmount.toAmountOrNull()
        if (amount == null || amount <= 0) {
            emitEffect(WalletEffect.ShowError("Geçerli bir tutar giriniz."))
            return
        }

        launchCoroutine {
            updateState { it.copy(isToppingUp = true, errorMessage = null) }
            when (val result = paymentUseCases.topUpWallet(amount)) {
                is NetworkResult.Success -> {
                    updateState {
                        it.copy(
                            isToppingUp = false,
                            isTopUpDialogVisible = false,
                            walletInfo = result.data,
                            topUpAmount = ""
                        )
                    }
                    emitEffect(WalletEffect.ShowMessage("Bakiye başarıyla yüklendi."))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isToppingUp = false, errorMessage = result.message) }
                    emitEffect(WalletEffect.ShowError(result.message))
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
                        it.resetCardForm(isVisible = false).copy(
                            savedCards = it.savedCards + newCard,
                            selectedCardToken = newCard.cardToken
                        )
                    }
                    emitEffect(WalletEffect.ShowMessage("Kart kaydedildi."))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isSavingCard = false, cardFormError = result.message) }
                    emitEffect(WalletEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun setDefaultCard(cardToken: String) {
        updateState { it.copy(selectedCardToken = cardToken) }
        launchCoroutine {
            when (val result = paymentUseCases.setDefaultCard(cardToken)) {
                is NetworkResult.Success -> {
                    val selected = result.data
                    updateState {
                        it.copy(
                            savedCards = it.savedCards.map { card ->
                                card.copy(isDefault = card.cardToken == selected.cardToken)
                            },
                            selectedCardToken = selected.cardToken
                        )
                    }
                    emitEffect(WalletEffect.ShowMessage("Varsayılan kart güncellendi."))
                }
                is NetworkResult.Error -> emitEffect(WalletEffect.ShowError(result.message))
            }
        }
    }

    private fun WalletState.resetCardForm(isVisible: Boolean): WalletState {
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
}
