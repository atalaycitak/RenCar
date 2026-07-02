package com.example.rencar_pair.presentation.ui.screens.trip_summary

import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class TripSummaryState(
    val rentalId: String = "",
    val rental: Rental? = null,
    val savedCards: List<PaymentMethod> = emptyList(),
    val selectedCardToken: String? = null,
    val isPaying: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface TripSummaryIntent : MviIntent {
    data class LoadSummary(val rentalId: String) : TripSummaryIntent
    data class SelectCard(val token: String) : TripSummaryIntent
    data object Pay : TripSummaryIntent
}

sealed interface TripSummaryEffect : MviEffect {
    data object NavigateToHome : TripSummaryEffect
    data class ShowPaymentSuccess(val message: String) : TripSummaryEffect
    data class ShowError(val message: String) : TripSummaryEffect
}
