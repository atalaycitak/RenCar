package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryChecklistViewModelTest {

    @Test
    fun `checklist cannot complete before all photos are taken`() {
        val viewModel = createViewModel()

        viewModel.onIntent(DeliveryChecklistIntent.TakeFrontPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.TakeBackPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.TakeLeftPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertEquals(3, viewModel.state.value.completedPhotoCount)
        assertFalse(viewModel.state.value.canComplete)
        assertFalse(viewModel.state.value.isCompleted)
    }

    @Test
    fun `checklist completes after all required photos are taken`() {
        val viewModel = createViewModel()

        viewModel.onIntent(DeliveryChecklistIntent.TakeFrontPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.TakeBackPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.TakeLeftPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.TakeRightPhoto)
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)

        assertEquals(4, viewModel.state.value.completedPhotoCount)
        assertTrue(viewModel.state.value.canComplete)
        assertTrue(viewModel.state.value.isCompleted)
    }

    private fun createViewModel(): DeliveryChecklistViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf("rentalId" to "rental-1", "vehicleId" to "vehicle-1")
        )
        return DeliveryChecklistViewModel(savedStateHandle)
    }
}
