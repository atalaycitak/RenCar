package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class DeliveryChecklistViewModel(
    savedStateHandle: SavedStateHandle
) : BaseMviViewModel<DeliveryChecklistState, DeliveryChecklistIntent, DeliveryChecklistEffect>(
    DeliveryChecklistState(
        rentalId = savedStateHandle.get<String>("rentalId") ?: "",
        vehicleId = savedStateHandle.get<String>("vehicleId") ?: ""
    )
) {

    override fun onIntent(intent: DeliveryChecklistIntent) {
        when (intent) {
            DeliveryChecklistIntent.ToggleVehicleCondition -> updateState {
                it.copy(vehicleConditionChecked = !it.vehicleConditionChecked)
            }
            DeliveryChecklistIntent.TogglePhotos -> updateState {
                it.copy(photosChecked = !it.photosChecked)
            }
            DeliveryChecklistIntent.ToggleDoorsAndKey -> updateState {
                it.copy(doorsAndKeyChecked = !it.doorsAndKeyChecked)
            }
            is DeliveryChecklistIntent.UpdateOdometer -> updateState {
                it.copy(odometerKm = intent.value.filter(Char::isDigit).take(7))
            }
            is DeliveryChecklistIntent.UpdateBatteryPercent -> updateState {
                it.copy(batteryPercent = intent.value.filter(Char::isDigit).take(3))
            }
            is DeliveryChecklistIntent.UpdateDamageNote -> updateState {
                it.copy(damageNote = intent.value.take(240))
            }
            DeliveryChecklistIntent.AddPhoto -> updateState {
                it.copy(photoCount = (it.photoCount + 1).coerceAtMost(12))
            }
            DeliveryChecklistIntent.RemovePhoto -> updateState {
                it.copy(photoCount = (it.photoCount - 1).coerceAtLeast(0))
            }
            DeliveryChecklistIntent.CompleteChecklist -> updateState {
                if (it.canComplete) it.copy(isCompleted = true) else it
            }
        }
    }
}
