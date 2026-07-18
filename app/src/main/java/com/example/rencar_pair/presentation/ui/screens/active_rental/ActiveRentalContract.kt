package com.example.rencar_pair.presentation.ui.screens.active_rental

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehiclePoint
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class ActiveRentalState(
    val activeRental: ActiveRental? = null,
    val rental: Rental? = null,
    val vehicle: Vehicle? = null,
    val elapsedSeconds: Long = 0,
    val distanceKm: Double = 0.0,
    val currentCost: Double = 0.0,
    val isVehicleLocked: Boolean = true,
    val isFinishing: Boolean = false,
    val showFinishConfirmation: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val routePoints: List<VehiclePoint> = emptyList(),
    val vehicleLocation: VehiclePoint? = null,
    val isGpsConnected: Boolean = false
) : MviState

sealed interface ActiveRentalIntent : MviIntent {
    data class LoadRental(val rentalId: String) : ActiveRentalIntent
    data object RequestFinishConfirmation : ActiveRentalIntent
    data object DismissFinishConfirmation : ActiveRentalIntent
    data object FinishRental : ActiveRentalIntent
    data object ToggleVehicleLock : ActiveRentalIntent
    data object TickTime : ActiveRentalIntent // Used for UI timer
}

sealed interface ActiveRentalEffect : MviEffect {
    data class NavigateToReturnVehicle(val rentalId: String) : ActiveRentalEffect
    data class NavigateToSummary(val rentalId: String) : ActiveRentalEffect
    data object NavigateToHome : ActiveRentalEffect
    data class ShowError(val message: String) : ActiveRentalEffect
}
