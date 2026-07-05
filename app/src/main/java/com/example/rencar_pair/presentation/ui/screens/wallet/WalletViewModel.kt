package com.example.rencar_pair.presentation.ui.screens.wallet

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.PaymentUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class WalletViewModel(
    private val paymentUseCases: PaymentUseCases
) : BaseMviViewModel<WalletState, WalletIntent, WalletEffect>(WalletState()) {

    private var defaultCardToken: String? = null

    init {
        onIntent(WalletIntent.LoadWallet)
    }

    override fun onIntent(intent: WalletIntent) {
        when (intent) {
            WalletIntent.LoadWallet -> loadWallet()
            WalletIntent.ShowTopUpDialog -> updateState {
                it.copy(isTopUpDialogVisible = true, topUpAmount = "")
            }
            WalletIntent.HideTopUpDialog -> updateState { it.copy(isTopUpDialogVisible = false) }
            is WalletIntent.UpdateTopUpAmount -> updateState {
                it.copy(topUpAmount = intent.amount)
            }
            WalletIntent.SubmitTopUp -> submitTopUp()
        }
    }

    private fun loadWallet() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = paymentUseCases.getWalletInfo()) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false, walletInfo = result.data) }
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
            loadDefaultCard()
        }
    }

    private fun loadDefaultCard() {
        launchCoroutine {
            when (val result = paymentUseCases.getSavedCards()) {
                is NetworkResult.Success -> {
                    defaultCardToken = result.data.firstOrNull()?.cardToken
                }
                is NetworkResult.Error -> { /* no saved cards yet, user will be notified on top-up */ }
            }
        }
    }

    private fun submitTopUp() {
        val amount = currentState().topUpAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            emitEffect(WalletEffect.ShowError("Geçerli bir tutar giriniz"))
            return
        }

        val cardToken = defaultCardToken
        if (cardToken == null) {
            emitEffect(WalletEffect.ShowError("Kayıtlı kart bulunamadı. Lütfen önce bir kart ekleyin."))
            return
        }

        launchCoroutine {
            updateState { it.copy(isToppingUp = true, errorMessage = null) }
            when (val result = paymentUseCases.topUpWallet(amount, cardToken)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isToppingUp = false, isTopUpDialogVisible = false) }
                    emitEffect(WalletEffect.ShowMessage("Bakiye başarıyla yüklendi!"))
                    loadWallet()
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isToppingUp = false, errorMessage = result.message) }
                    emitEffect(WalletEffect.ShowError(result.message))
                }
            }
        }
    }
}
