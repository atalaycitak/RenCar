package com.example.rencar_pair.presentation.ui.screens.history

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class TripHistoryState(
    val rentals: List<Rental> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState {
    val totalSpent: Double
        get() = rentals.sumOf { it.totalPrice ?: 0.0 }
}

sealed interface TripHistoryIntent : MviIntent {
    data object LoadHistory : TripHistoryIntent
}
