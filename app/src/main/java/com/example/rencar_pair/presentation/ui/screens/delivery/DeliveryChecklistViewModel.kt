package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DeliveryChecklistViewModel(
    rentalId: String,
    vehicleId: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        DeliveryChecklistState(
            rentalId = rentalId,
            vehicleId = vehicleId
        )
    )
    val state = _state.asStateFlow()

    fun onIntent(intent: DeliveryChecklistIntent) {
        when (intent) {
            DeliveryChecklistIntent.ToggleVehicleCondition -> _state.update {
                it.copy(vehicleConditionChecked = !it.vehicleConditionChecked)
            }
            DeliveryChecklistIntent.TogglePhotos -> _state.update {
                it.copy(photosChecked = !it.photosChecked)
            }
            DeliveryChecklistIntent.ToggleDoorsAndKey -> _state.update {
                it.copy(doorsAndKeyChecked = !it.doorsAndKeyChecked)
            }
            DeliveryChecklistIntent.CompleteChecklist -> _state.update {
                if (it.canComplete) it.copy(isCompleted = true) else it
            }
        }
    }
}
