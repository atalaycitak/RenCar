package com.example.rencar_pair.presentation.ui.screens.trip_summary

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class TripSummaryState(
    val rentalId: String = "",
    val rental: Rental? = null,
    val savedCards: List<PaymentMethod> = emptyList(),
    val selectedCardToken: String? = null,
    val isAddCardDialogVisible: Boolean = false,
    val isSavingCard: Boolean = false,
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val cardFormError: String? = null,
    val isPaying: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface TripSummaryIntent : MviIntent {
    data class LoadSummary(val rentalId: String) : TripSummaryIntent
    data class SelectCard(val token: String) : TripSummaryIntent
    data object ShowAddCardDialog : TripSummaryIntent
    data object HideAddCardDialog : TripSummaryIntent
    data class UpdateCardHolderName(val value: String) : TripSummaryIntent
    data class UpdateCardNumber(val value: String) : TripSummaryIntent
    data class UpdateCardExpiry(val value: String) : TripSummaryIntent
    data class UpdateCardCvc(val value: String) : TripSummaryIntent
    data object SubmitCard : TripSummaryIntent
    data object Pay : TripSummaryIntent
}

sealed interface TripSummaryEffect : MviEffect {
    data object NavigateToHome : TripSummaryEffect
    data class ShowPaymentSuccess(val message: String) : TripSummaryEffect
    data class ShowError(val message: String) : TripSummaryEffect
}
