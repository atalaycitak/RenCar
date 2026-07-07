package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.compose.runtime.Stable
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class DeliveryChecklistState(
    val rentalId: String,
    val vehicleId: String,
    val vehicleConditionChecked: Boolean = false,
    val photosChecked: Boolean = false,
    val doorsAndKeyChecked: Boolean = false,
    val odometerKm: String = "",
    val batteryPercent: String = "",
    val damageNote: String = "",
    val photoCount: Int = 0,
    val isCompleted: Boolean = false
) : MviState {
    val hasDeliveryDetails: Boolean
        get() = odometerKm.toIntOrNull()?.let { it >= 0 } == true &&
            batteryPercent.toIntOrNull()?.let { it in 0..100 } == true &&
            photoCount >= 4

    val canComplete: Boolean
        get() = vehicleConditionChecked && photosChecked && doorsAndKeyChecked && hasDeliveryDetails
}

sealed interface DeliveryChecklistIntent : MviIntent {
    data object ToggleVehicleCondition : DeliveryChecklistIntent
    data object TogglePhotos : DeliveryChecklistIntent
    data object ToggleDoorsAndKey : DeliveryChecklistIntent
    data class UpdateOdometer(val value: String) : DeliveryChecklistIntent
    data class UpdateBatteryPercent(val value: String) : DeliveryChecklistIntent
    data class UpdateDamageNote(val value: String) : DeliveryChecklistIntent
    data object AddPhoto : DeliveryChecklistIntent
    data object RemovePhoto : DeliveryChecklistIntent
    data object CompleteChecklist : DeliveryChecklistIntent
}

sealed interface DeliveryChecklistEffect : MviEffect
