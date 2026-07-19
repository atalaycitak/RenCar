package com.example.rencar_pair.presentation.ui.screens.home

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleDistanceInfo
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleLocationStreamMode
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class HomeState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: String? = null,
    val selectedVehicleType: VehicleType? = null,
    val maxDailyPrice: Int? = null,
    val minRangeKm: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locationPermissionGranted: Boolean = false,
    val userLocation: UserLocation? = null,
    val activeReservation: Reservation? = null,
    val activeRental: ActiveRental? = null,
    val pendingRental: Rental? = null,
    val hasLiveVehicleUpdates: Boolean = false,
    val vehicleLocationStreamMode: VehicleLocationStreamMode = VehicleLocationStreamMode.Inactive,
    
    // Computed UI Data (populated by ViewModel via UseCase)
    val filteredVehicles: List<Vehicle> = emptyList(),
    val visibleVehicles: List<Vehicle> = emptyList(),
    val nearbyVehicles: List<Vehicle> = emptyList(),
    val actionableNearbyVehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val activeReservationVehicle: Vehicle? = null,
    val activeRentalVehicle: Vehicle? = null,
    val pendingRentalVehicle: Vehicle? = null,
    val highlightedVehicle: Vehicle? = null,
    val distanceInfoMap: Map<String, VehicleDistanceInfo> = emptyMap()
) : MviState {
    val isReservationLocked: Boolean
        get() = activeReservationVehicle != null && activeRental == null

    val hasActiveFilters: Boolean
        get() = selectedVehicleType != null || maxDailyPrice != null || minRangeKm != null
}

sealed interface HomeIntent : MviIntent {
    data object LoadVehicles : HomeIntent
    data object LoadActiveReservation : HomeIntent
    data object LoadActiveRental : HomeIntent
    data object LoadPendingRental : HomeIntent
    data class SelectVehicle(val id: String?) : HomeIntent
    data class UpdateVehicleTypeFilter(val type: VehicleType?) : HomeIntent
    data class UpdateMaxPriceFilter(val maxPrice: Int?) : HomeIntent
    data class UpdateMinRangeFilter(val minRangeKm: Int?) : HomeIntent
    data object ClearFilters : HomeIntent
    data class LocationPermissionChanged(val granted: Boolean) : HomeIntent
    data object FetchUserLocation : HomeIntent
    data object FocusUserLocation : HomeIntent
}

