package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class ActiveRentalState(
    val rental: Rental? = null,
    val elapsedMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val currentCost: Double = 0.0,
    val isFinishing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface ActiveRentalIntent : MviIntent {
    data class LoadRental(val rentalId: String) : ActiveRentalIntent
    data object FinishRental : ActiveRentalIntent
    data object TickTime : ActiveRentalIntent // Used for UI timer
}

sealed interface ActiveRentalEffect : MviEffect {
    data class NavigateToReturnVehicle(val rentalId: String) : ActiveRentalEffect
    data class ShowError(val message: String) : ActiveRentalEffect
}
