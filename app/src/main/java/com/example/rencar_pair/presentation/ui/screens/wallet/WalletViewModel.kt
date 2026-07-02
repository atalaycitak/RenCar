package com.example.rencar_pair.presentation.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.payment.GetWalletInfoUseCase
import com.example.rencar_pair.domain.usecase.payment.TopUpWalletUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel(
    private val getWalletInfoUseCase: GetWalletInfoUseCase,
    private val topUpWalletUseCase: TopUpWalletUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state = _state.asStateFlow()

    private val _effect = Channel<WalletEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(WalletIntent.LoadWallet)
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            WalletIntent.LoadWallet -> loadWallet()
            WalletIntent.ShowTopUpDialog -> _state.update { it.copy(isTopUpDialogVisible = true, topUpAmount = "") }
            WalletIntent.HideTopUpDialog -> _state.update { it.copy(isTopUpDialogVisible = false) }
            is WalletIntent.UpdateTopUpAmount -> _state.update { it.copy(topUpAmount = intent.amount) }
            WalletIntent.SubmitTopUp -> submitTopUp()
        }
    }

    private fun loadWallet() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getWalletInfoUseCase()) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false, walletInfo = result.data) }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun submitTopUp() {
        val amount = state.value.topUpAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            viewModelScope.launch { _effect.send(WalletEffect.ShowError("Geçerli bir tutar giriniz")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isToppingUp = true, errorMessage = null) }
            // Mock card token for now, since we haven't implemented select card for top up
            val mockCardToken = "tok_12345"
            when (val result = topUpWalletUseCase(amount, mockCardToken)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isToppingUp = false, isTopUpDialogVisible = false) }
                    _effect.send(WalletEffect.ShowMessage("Bakiye başarıyla yüklendi!"))
                    loadWallet() // Refresh
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isToppingUp = false, errorMessage = result.message) }
                    _effect.send(WalletEffect.ShowError(result.message ?: "Yükleme başarısız oldu"))
                }
                NetworkResult.Loading -> Unit
            }
        }
    }
}
