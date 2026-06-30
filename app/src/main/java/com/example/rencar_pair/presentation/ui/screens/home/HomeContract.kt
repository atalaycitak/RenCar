package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class HomeState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicleId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locationPermissionGranted: Boolean = false
) : MviState {
    val selectedVehicle: Vehicle? = vehicles.firstOrNull { it.id == selectedVehicleId } ?: vehicles.firstOrNull()
}

sealed interface HomeIntent : MviIntent {
    data object LoadVehicles : HomeIntent
    data class SelectVehicle(val id: String) : HomeIntent
    data class LocationPermissionChanged(val granted: Boolean) : HomeIntent
}
