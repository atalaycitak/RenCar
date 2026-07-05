package com.example.rencar_pair.presentation.ui.screens.home

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleType
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
    val userLocation: UserLocation? = null
) : MviState {
    val filteredVehicles: List<Vehicle>
        get() = vehicles.filter { vehicle ->
            val matchesType = selectedVehicleType == null || vehicle.type == selectedVehicleType
            val matchesPrice = maxDailyPrice == null || vehicle.pricePerDay <= maxDailyPrice.toDouble()
            val matchesRange = minRangeKm == null || vehicle.rangeKm >= minRangeKm
            matchesType && matchesPrice && matchesRange
        }

    val selectedVehicle: Vehicle?
        get() = filteredVehicles.firstOrNull { it.id == selectedVehicleId }

    val hasActiveFilters: Boolean
        get() = selectedVehicleType != null || maxDailyPrice != null || minRangeKm != null
}

sealed interface HomeIntent : MviIntent {
    data object LoadVehicles : HomeIntent
    data class SelectVehicle(val id: String?) : HomeIntent
    data class UpdateVehicleTypeFilter(val type: VehicleType?) : HomeIntent
    data class UpdateMaxPriceFilter(val maxPrice: Int?) : HomeIntent
    data class UpdateMinRangeFilter(val minRangeKm: Int?) : HomeIntent
    data object ClearFilters : HomeIntent
    data class LocationPermissionChanged(val granted: Boolean) : HomeIntent
    data object FetchUserLocation : HomeIntent
}
