package com.example.rencar_pair.presentation.ui.screens.vehicle

import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface VehicleDetailIntent : MviIntent {
    data object LoadVehicle : VehicleDetailIntent
}
