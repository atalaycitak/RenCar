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
            is WalletIntent.SelectCard -> updateState { it.copy(selectedCardToken = intent.token) }
            WalletIntent.ShowTopUpDialog -> showTopUpDialog()
            WalletIntent.HideTopUpDialog -> updateState {
                it.copy(isTopUpDialogVisible = false, isToppingUp = false, topUpAmount = "")
            }
            is WalletIntent.UpdateTopUpAmount -> updateState {
                it.copy(topUpAmount = intent.amount.asAmountInput())
            }
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
        val card = currentState().defaultCard
        if (card == null) {
            emitEffect(WalletEffect.ShowError("Bakiye yuklemek icin once kart ekleyin."))
            return
        }
        updateState { it.copy(isTopUpDialogVisible = true, topUpAmount = "") }
    }

    private fun submitTopUp() {
        val current = currentState()
        val amount = current.topUpAmount.toAmountOrNull()
        val card = current.defaultCard
        if (amount == null || amount <= 0) {
            emitEffect(WalletEffect.ShowError("Gecerli bir tutar giriniz."))
            return
        }
        if (card == null) {
            emitEffect(WalletEffect.ShowError("Bakiye yuklemek icin once kart ekleyin."))
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
                            topUpAmount = ""
                        )
                    }
                    emitEffect(WalletEffect.ShowMessage("Bakiye basariyla yuklendi."))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isToppingUp = false, errorMessage = result.message) }
                    emitEffect(WalletEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun String.asAmountInput(): String {
        return filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
            .take(9)
    }

    private fun String.toAmountOrNull(): Double? = replace(',', '.').toDoubleOrNull()
}
