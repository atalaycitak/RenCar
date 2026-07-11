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
