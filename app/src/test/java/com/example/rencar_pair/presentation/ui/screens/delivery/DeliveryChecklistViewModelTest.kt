package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryChecklistViewModelTest {

    @Test
    fun `checklist cannot complete before all steps are checked`() {
        val savedStateHandle = SavedStateHandle(mapOf("rentalId" to "rental-1", "vehicleId" to "vehicle-1"))
        val viewModel = DeliveryChecklistViewModel(savedStateHandle)

        viewModel.onIntent(DeliveryChecklistIntent.ToggleVehicleCondition)
        viewModel.onIntent(DeliveryChecklistIntent.TogglePhotos)
        viewModel.onIntent(DeliveryChecklistIntent.ToggleDoorsAndKey)
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertFalse(viewModel.state.value.isCompleted)
    }

    @Test
    fun `checklist completes after all delivery steps are checked`() {
        val savedStateHandle = SavedStateHandle(mapOf("rentalId" to "rental-1", "vehicleId" to "vehicle-1"))
        val viewModel = DeliveryChecklistViewModel(savedStateHandle)

        viewModel.onIntent(DeliveryChecklistIntent.ToggleVehicleCondition)
        viewModel.onIntent(DeliveryChecklistIntent.TogglePhotos)
        viewModel.onIntent(DeliveryChecklistIntent.ToggleDoorsAndKey)
        viewModel.onIntent(DeliveryChecklistIntent.UpdateOdometer("12450"))
        viewModel.onIntent(DeliveryChecklistIntent.UpdateBatteryPercent("82"))
        repeat(4) {
            viewModel.onIntent(DeliveryChecklistIntent.AddPhoto)
        }
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertTrue(viewModel.state.value.isCompleted)
    }

    @Test
    fun `battery percent and odometer inputs keep numeric values`() {
        val savedStateHandle = SavedStateHandle(mapOf("rentalId" to "rental-1", "vehicleId" to "vehicle-1"))
        val viewModel = DeliveryChecklistViewModel(savedStateHandle)

        viewModel.onIntent(DeliveryChecklistIntent.UpdateOdometer("12a450km"))
        viewModel.onIntent(DeliveryChecklistIntent.UpdateBatteryPercent("82%"))

        assertTrue(viewModel.state.value.odometerKm == "12450")
        assertTrue(viewModel.state.value.batteryPercent == "82")
    }
}
