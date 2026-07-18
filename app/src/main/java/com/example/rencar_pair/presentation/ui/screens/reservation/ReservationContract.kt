package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.ReservationQuote
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class ReservationState(
    val vehicle: Vehicle? = null,
    val quote: ReservationQuote? = null,
    val activeReservation: Reservation? = null,
    val rentalId: String? = null,
    val selectedPlan: RentalPlan = RentalPlan.PerMinute,
    val selectedDays: Int = 1,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) : MviState {
    val canUnlockSelectedVehicle: Boolean
        get() = vehicle?.id != null && activeReservation?.vehicleId == vehicle.id

    val canSubmit: Boolean
        get() = vehicle != null && termsAccepted && !isLoading && !isSubmitting
}

sealed interface ReservationIntent : MviIntent {
    data object LoadVehicle : ReservationIntent
    data object IncreaseDays : ReservationIntent
    data object DecreaseDays : ReservationIntent
    data class SelectDays(val days: Int) : ReservationIntent
    data class SelectPlan(val plan: RentalPlan) : ReservationIntent
    data object ToggleTermsAccepted : ReservationIntent
    data object ConfirmReservation : ReservationIntent
}

sealed interface ReservationEffect : MviEffect {
    data class NavigateToDelivery(val rentalId: String, val vehicleId: String) : ReservationEffect
    data class NavigateToActiveRental(val rentalId: String) : ReservationEffect
}
