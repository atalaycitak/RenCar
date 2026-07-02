package com.example.rencar_pair.presentation.ui.screens.wallet

import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class WalletState(
    val walletInfo: WalletInfo? = null,
    val isLoading: Boolean = false,
    val isTopUpDialogVisible: Boolean = false,
    val isToppingUp: Boolean = false,
    val topUpAmount: String = "",
    val errorMessage: String? = null
) : MviState

sealed interface WalletIntent : MviIntent {
    data object LoadWallet : WalletIntent
    data object ShowTopUpDialog : WalletIntent
    data object HideTopUpDialog : WalletIntent
    data class UpdateTopUpAmount(val amount: String) : WalletIntent
    data object SubmitTopUp : WalletIntent
}

sealed interface WalletEffect : MviEffect {
    data class ShowMessage(val message: String) : WalletEffect
    data class ShowError(val message: String) : WalletEffect
}
