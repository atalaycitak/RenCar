package com.example.rencar_pair.presentation.ui.screens.history

import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class TripHistoryState(
    val rentals: List<Rental> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface TripHistoryIntent : MviIntent {
    data object LoadHistory : TripHistoryIntent
}

sealed interface TripHistoryEffect : MviEffect
