package com.example.rencar_pair.presentation.ui.screens.delivery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryChecklistViewModelTest {

    @Test
    fun `checklist cannot complete before all steps are checked`() {
        val viewModel = DeliveryChecklistViewModel(
            rentalId = "rental-1",
            vehicleId = "vehicle-1"
        )

        viewModel.onIntent(DeliveryChecklistIntent.ToggleVehicleCondition)
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertFalse(viewModel.state.value.isCompleted)
    }

    @Test
    fun `checklist completes after all delivery steps are checked`() {
        val viewModel = DeliveryChecklistViewModel(
            rentalId = "rental-1",
            vehicleId = "vehicle-1"
        )

        viewModel.onIntent(DeliveryChecklistIntent.ToggleVehicleCondition)
        viewModel.onIntent(DeliveryChecklistIntent.TogglePhotos)
        viewModel.onIntent(DeliveryChecklistIntent.ToggleDoorsAndKey)
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertTrue(viewModel.state.value.isCompleted)
    }
}
