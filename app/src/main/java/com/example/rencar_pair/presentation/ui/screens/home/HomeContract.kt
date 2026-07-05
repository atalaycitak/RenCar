package com.example.rencar_pair.presentation.ui.screens.home

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class HomeState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locationPermissionGranted: Boolean = false,
    val userLocation: UserLocation? = null
) : MviState {
    val selectedVehicle: Vehicle? = vehicles.firstOrNull { it.id == selectedVehicleId }
}

sealed interface HomeIntent : MviIntent {
    data object LoadVehicles : HomeIntent
    data class SelectVehicle(val id: String?) : HomeIntent
    data class LocationPermissionChanged(val granted: Boolean) : HomeIntent
    data object FetchUserLocation : HomeIntent
}
