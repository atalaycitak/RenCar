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
            DeliveryChecklistIntent.CompleteChecklist -> updateState {
                if (it.canComplete) it.copy(isCompleted = true) else it
            }
        }
    }
}
