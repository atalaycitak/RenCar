package com.example.rencar_pair.presentation.ui.screens.delivery

import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class DeliveryChecklistState(
    val rentalId: String,
    val vehicleId: String,
    val vehicleConditionChecked: Boolean = false,
    val photosChecked: Boolean = false,
    val doorsAndKeyChecked: Boolean = false,
    val isCompleted: Boolean = false
) : MviState {
    val canComplete: Boolean = vehicleConditionChecked && photosChecked && doorsAndKeyChecked
}

sealed interface DeliveryChecklistIntent : MviIntent {
    data object ToggleVehicleCondition : DeliveryChecklistIntent
    data object TogglePhotos : DeliveryChecklistIntent
    data object ToggleDoorsAndKey : DeliveryChecklistIntent
    data object CompleteChecklist : DeliveryChecklistIntent
}

sealed interface DeliveryChecklistEffect : MviEffect
