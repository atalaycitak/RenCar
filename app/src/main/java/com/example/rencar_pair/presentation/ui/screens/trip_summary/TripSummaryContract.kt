package com.example.rencar_pair.presentation.ui.screens.trip_summary

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class TripSummaryState(
    val rentalId: String = "",
    val rental: Rental? = null,
    val walletInfo: WalletInfo? = null,
    val savedCards: List<SavedCard> = emptyList(),
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.Wallet,
    val selectedCardToken: String? = null,
    val isAddCardDialogVisible: Boolean = false,
    val isTopUpDialogVisible: Boolean = false,
    val isSavingCard: Boolean = false,
    val isToppingUp: Boolean = false,
    val topUpAmount: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val cardFormError: String? = null,
    val isPaying: Boolean = false,
    val isLoading: Boolean = false,
    val paymentResult: PaymentResult? = null,
    val errorMessage: String? = null
) : MviState {
    val totalPrice: Double
        get() = rental?.totalPrice ?: 0.0

    val walletBalance: Double
        get() = walletInfo?.balance ?: 0.0

    val selectedCard: SavedCard?
        get() = savedCards.firstOrNull { it.cardToken == selectedCardToken }
            ?: savedCards.firstOrNull { it.isDefault }
            ?: savedCards.firstOrNull()

    val defaultCard: SavedCard?
        get() = savedCards.firstOrNull { it.isDefault } ?: savedCards.firstOrNull()

    val walletShortfall: Double
        get() = (totalPrice - walletBalance).coerceAtLeast(0.0)
}

sealed interface TripSummaryIntent : MviIntent {
    data class LoadSummary(val rentalId: String) : TripSummaryIntent
    data class SelectPaymentMethod(val method: PaymentMethod) : TripSummaryIntent
    data class SelectCard(val token: String) : TripSummaryIntent
    data object ShowAddCardDialog : TripSummaryIntent
    data object HideAddCardDialog : TripSummaryIntent
    data object ShowTopUpDialog : TripSummaryIntent
    data object HideTopUpDialog : TripSummaryIntent
    data class UpdateTopUpAmount(val amount: String) : TripSummaryIntent
    data class UpdateCardHolderName(val value: String) : TripSummaryIntent
    data class UpdateCardNumber(val value: String) : TripSummaryIntent
    data class UpdateCardExpiry(val value: String) : TripSummaryIntent
    data class UpdateCardCvc(val value: String) : TripSummaryIntent
    data object SubmitCard : TripSummaryIntent
    data object SubmitTopUp : TripSummaryIntent
    data object Pay : TripSummaryIntent
}

sealed interface TripSummaryEffect : MviEffect {
    data object NavigateToHome : TripSummaryEffect
    data class ShowPaymentSuccess(val message: String) : TripSummaryEffect
    data class ShowError(val message: String) : TripSummaryEffect
}
