package com.example.rencar_pair.presentation.ui.screens.return_vehicle

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.ReturnAngle
import com.example.rencar_pair.domain.repository.RentalRepository
import com.example.rencar_pair.domain.usecase.rental.ReturnVehicleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ReturnVehicleViewModelTest {

    @get:Rule
    val mainDispatcherRule = ReturnVehicleMainDispatcherRule()

    @Test
    fun `request confirmation requires four photos`() {
        val viewModel = createViewModel()

        viewModel.onIntent(ReturnVehicleIntent.AddPhoto(ReturnAngle.FRONT, "front-uri"))
        viewModel.onIntent(ReturnVehicleIntent.RequestReturnConfirmation)

        assertFalse(viewModel.state.value.showReturnConfirmation)
        assertEquals("Lutfen dort acidan da fotograf yukleyin", viewModel.state.value.errorMessage)
    }

    @Test
    fun `request confirmation opens when all photos are present`() {
        val viewModel = createViewModel()

        addAllPhotos(viewModel)
        viewModel.onIntent(ReturnVehicleIntent.RequestReturnConfirmation)

        assertTrue(viewModel.state.value.showReturnConfirmation)
    }

    @Test
    fun `submit return sends damage note and emits summary navigation`() = runTest {
        val repository = FakeRentalRepositoryForReturnTest()
        val viewModel = createViewModel(repository)

        addAllPhotos(viewModel)
        viewModel.onIntent(ReturnVehicleIntent.UpdateDamageNote("Sag arka tampon cizik"))
        viewModel.onIntent(ReturnVehicleIntent.SubmitReturn("rental-1"))
        advanceUntilIdle()

        val effect = viewModel.effect.first()

        assertEquals("rental-1", repository.returnedRentalId)
        assertEquals("Sag arka tampon cizik", repository.damageNote)
        assertTrue(effect is ReturnVehicleEffect.NavigateToSummary)
        assertEquals("rental-1", (effect as ReturnVehicleEffect.NavigateToSummary).rentalId)
    }

    private fun createViewModel(
        repository: RentalRepository = FakeRentalRepositoryForReturnTest()
    ): ReturnVehicleViewModel {
        return ReturnVehicleViewModel(ReturnVehicleUseCase(repository))
    }

    private fun addAllPhotos(viewModel: ReturnVehicleViewModel) {
        viewModel.onIntent(ReturnVehicleIntent.AddPhoto(ReturnAngle.FRONT, "front-uri"))
        viewModel.onIntent(ReturnVehicleIntent.AddPhoto(ReturnAngle.BACK, "back-uri"))
        viewModel.onIntent(ReturnVehicleIntent.AddPhoto(ReturnAngle.LEFT, "left-uri"))
        viewModel.onIntent(ReturnVehicleIntent.AddPhoto(ReturnAngle.RIGHT, "right-uri"))
    }
}

private class FakeRentalRepositoryForReturnTest : RentalRepository {
    var returnedRentalId: String? = null
    var damageNote: String? = null

    override suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit> {
        returnedRentalId = rentalId
        this.damageNote = damageNote
        return if (photos.size == 4) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error("Missing photos")
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReturnVehicleMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
