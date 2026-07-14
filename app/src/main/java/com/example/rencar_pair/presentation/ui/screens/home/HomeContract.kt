package com.example.rencar_pair.presentation.ui.screens.home

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleLocationStreamMode
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

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
    val hasLiveVehicleUpdates: Boolean = false,
    val vehicleLocationStreamMode: VehicleLocationStreamMode = VehicleLocationStreamMode.Inactive
) : MviState {
    val filteredVehicles: List<Vehicle>
        get() = vehicles.filter { vehicle ->
            val matchesType = selectedVehicleType == null || vehicle.type == selectedVehicleType
            val matchesPrice = maxDailyPrice == null || vehicle.pricePerDay <= maxDailyPrice.toDouble()
            val matchesRange = minRangeKm == null || vehicle.rangeKm >= minRangeKm
            matchesType && matchesPrice && matchesRange
        }

    val nearbyVehicles: List<Vehicle>
        get() = userLocation?.let { location ->
            filteredVehicles.sortedBy { vehicle -> location.distanceKmTo(vehicle) }
        } ?: filteredVehicles

    val selectedVehicle: Vehicle?
        get() = filteredVehicles.firstOrNull { it.id == selectedVehicleId }

    val highlightedVehicle: Vehicle?
        get() = selectedVehicle ?: nearbyVehicles.firstOrNull()

    val hasActiveFilters: Boolean
        get() = selectedVehicleType != null || maxDailyPrice != null || minRangeKm != null

    fun distanceKmTo(vehicle: Vehicle): Double? {
        return userLocation?.distanceKmTo(vehicle)
    }

    fun distanceInfoFor(vehicle: Vehicle): VehicleDistanceInfo? {
        val distanceKm = distanceKmTo(vehicle) ?: return null
        return VehicleDistanceInfo(
            distanceKm = distanceKm,
            walkingMinutes = ((distanceKm / WALKING_SPEED_KM_PER_HOUR) * MINUTES_PER_HOUR)
                .roundToInt()
                .coerceAtLeast(1)
        )
    }
}

data class VehicleDistanceInfo(
    val distanceKm: Double,
    val walkingMinutes: Int
) {
    val distanceLabel: String
        get() = if (distanceKm < 1.0) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            "${(distanceKm * 10).roundToInt() / 10.0} km"
        }
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
    data object FocusUserLocation : HomeIntent
}

private fun UserLocation.distanceKmTo(vehicle: Vehicle): Double {
    val latitudeDelta = Math.toRadians(vehicle.latitude - latitude)
    val longitudeDelta = Math.toRadians(vehicle.longitude - longitude)
    val originLatitude = Math.toRadians(latitude)
    val targetLatitude = Math.toRadians(vehicle.latitude)

    val haversine = sin(latitudeDelta / 2).pow(2.0) +
        cos(originLatitude) * cos(targetLatitude) * sin(longitudeDelta / 2).pow(2.0)
    val centralAngle = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    return EARTH_RADIUS_KM * centralAngle
}

private const val EARTH_RADIUS_KM = 6371.0
private const val WALKING_SPEED_KM_PER_HOUR = 4.8
private const val MINUTES_PER_HOUR = 60
