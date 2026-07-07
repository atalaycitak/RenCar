package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.ReservationQuote
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class ReservationState(
    val vehicle: Vehicle? = null,
    val quote: ReservationQuote? = null,
    val rentalId: String? = null,
    val selectedDays: Int = 1,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface ReservationIntent : MviIntent {
    data object LoadVehicle : ReservationIntent
    data object IncreaseDays : ReservationIntent
    data object DecreaseDays : ReservationIntent
    data class SelectDays(val days: Int) : ReservationIntent
    data object ConfirmReservation : ReservationIntent
}

sealed interface ReservationEffect : MviEffect {
    data class NavigateToDelivery(val rentalId: String, val vehicleId: String) : ReservationEffect
}
