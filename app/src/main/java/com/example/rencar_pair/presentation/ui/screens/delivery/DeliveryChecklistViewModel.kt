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
            DeliveryChecklistIntent.TakeFrontPhoto -> updateState {
                it.copy(frontPhotoTaken = true)
            }
            DeliveryChecklistIntent.TakeBackPhoto -> updateState {
                it.copy(backPhotoTaken = true)
            }
            DeliveryChecklistIntent.TakeLeftPhoto -> updateState {
                it.copy(leftPhotoTaken = true)
            }
            DeliveryChecklistIntent.TakeRightPhoto -> updateState {
                it.copy(rightPhotoTaken = true)
            }
            DeliveryChecklistIntent.CompleteChecklist -> updateState {
                if (it.canComplete) it.copy(isCompleted = true) else it
            }
        }
    }
}
