package com.example.rencar_pair.presentation.ui.screens.wallet

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class WalletState(
    val walletInfo: WalletInfo? = null,
    val savedCards: List<SavedCard> = emptyList(),
    val selectedCardToken: String? = null,
    val isLoading: Boolean = false,
    val isTopUpDialogVisible: Boolean = false,
    val isAddCardDialogVisible: Boolean = false,
    val isToppingUp: Boolean = false,
    val isSavingCard: Boolean = false,
    val topUpAmount: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val cardFormError: String? = null,
    val errorMessage: String? = null
) : MviState {
    val defaultCard: SavedCard?
        get() = savedCards.firstOrNull { it.isDefault }
            ?: savedCards.firstOrNull { it.cardToken == selectedCardToken }
            ?: savedCards.firstOrNull()
}

sealed interface WalletIntent : MviIntent {
    data object LoadWallet : WalletIntent
    data class SelectCard(val token: String) : WalletIntent
    data object ShowTopUpDialog : WalletIntent
    data object HideTopUpDialog : WalletIntent
    data object ShowAddCardDialog : WalletIntent
    data object HideAddCardDialog : WalletIntent
    data class UpdateTopUpAmount(val amount: String) : WalletIntent
    data class UpdateCardHolderName(val value: String) : WalletIntent
    data class UpdateCardNumber(val value: String) : WalletIntent
    data class UpdateCardExpiry(val value: String) : WalletIntent
    data class UpdateCardCvc(val value: String) : WalletIntent
    data object SubmitCard : WalletIntent
    data object SubmitTopUp : WalletIntent
}

sealed interface WalletEffect : MviEffect {
    data class ShowMessage(val message: String) : WalletEffect
    data class ShowError(val message: String) : WalletEffect
}
