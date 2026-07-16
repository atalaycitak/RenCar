package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.repository.RentalRepository
import com.example.rencar_pair.domain.usecase.rental.RentalPhotoUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryChecklistViewModelTest {

    @get:Rule
    val mainDispatcherRule = DeliveryChecklistMainDispatcherRule()

    @Test
    fun `checklist cannot complete before all photos are uploaded`() = runTest {
        val repository = FakeRentalRepositoryForDeliveryTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Front, "content://front"))
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Back, "content://back"))
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Left, "content://left"))
        advanceUntilIdle()
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.completedPhotoCount)
        assertFalse(viewModel.state.value.canComplete)
        assertFalse(viewModel.state.value.isCompleted)
        assertEquals(false, repository.startCalled)
    }

    @Test
    fun `checklist uploads four photos and starts rental`() = runTest {
        val repository = FakeRentalRepositoryForDeliveryTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val effect = async { viewModel.effect.first() }
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Front, "content://front"))
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Back, "content://back"))
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Left, "content://left"))
        viewModel.onIntent(DeliveryChecklistIntent.SelectPhoto(RentalPhotoSide.Right, "content://right"))
        advanceUntilIdle()
        viewModel.onIntent(DeliveryChecklistIntent.CompleteChecklist)
        advanceUntilIdle()

        assertEquals(4, viewModel.state.value.completedPhotoCount)
        assertTrue(viewModel.state.value.canComplete)
        assertTrue(viewModel.state.value.isCompleted)
        assertEquals(true, repository.startCalled)
        assertEquals(DeliveryChecklistEffect.ChecklistCompleted, effect.await())
    }

    private fun createViewModel(
        repository: RentalRepository = FakeRentalRepositoryForDeliveryTest()
    ): DeliveryChecklistViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf("rentalId" to "rental-1", "vehicleId" to "vehicle-1")
        )
        return DeliveryChecklistViewModel(
            savedStateHandle = savedStateHandle,
            rentalPhotoUseCases = RentalPhotoUseCases(repository)
        )
    }
}

private class FakeRentalRepositoryForDeliveryTest : RentalRepository {
    private val uploadedSides = linkedSetOf<RentalPhotoSide>()
    var startCalled: Boolean = false

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> {
        return NetworkResult.Success(photoState(rentalId))
    }

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        uploadedSides += side
        return NetworkResult.Success(photoState(rentalId))
    }

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> {
        startCalled = true
        return if (uploadedSides.size == 4) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error("${4 - uploadedSides.size} foto kaldı")
        }
    }

    override suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit> {
        return NetworkResult.Success(Unit)
    }

    private fun photoState(rentalId: String): RentalPhotosState {
        val remaining = RentalPhotoSide.entries.toSet() - uploadedSides
        return RentalPhotosState(
            rentalId = rentalId,
            uploadedSides = uploadedSides.toSet(),
            remainingSides = remaining,
            photosComplete = remaining.isEmpty()
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryChecklistMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
