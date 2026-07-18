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

    override suspend fun createRental(
        vehicleId: String,
        plan: com.example.rencar_pair.domain.model.RentalPlan?,
        endDate: String?
    ): NetworkResult<com.example.rencar_pair.domain.model.Rental> = NetworkResult.Error("Not implemented")

    override suspend fun getMyRentals(): NetworkResult<List<com.example.rencar_pair.domain.model.Rental>> = NetworkResult.Error("Not implemented")

    override suspend fun getRental(id: String): NetworkResult<com.example.rencar_pair.domain.model.Rental> = NetworkResult.Error("Not implemented")

    override suspend fun getActiveRental(): NetworkResult<com.example.rencar_pair.domain.model.ActiveRental?> = NetworkResult.Error("Not implemented")

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<com.example.rencar_pair.domain.model.RentalPhotosState> = NetworkResult.Error("Not implemented")

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: com.example.rencar_pair.domain.model.RentalPhotoSide,
        photoUri: String
    ): NetworkResult<com.example.rencar_pair.domain.model.RentalPhotosState> = NetworkResult.Error("Not implemented")

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> = NetworkResult.Error("Not implemented")

    override suspend fun finishRental(rentalId: String): NetworkResult<com.example.rencar_pair.domain.model.FinishedRental> = NetworkResult.Error("Not implemented")

    override suspend fun payRental(
        rentalId: String,
        method: com.example.rencar_pair.domain.model.PaymentMethod,
        cardId: String?,
        discountCode: String?
    ): NetworkResult<Unit> = NetworkResult.Error("Not implemented")

    override suspend fun cancelRental(rentalId: String): NetworkResult<Unit> {
        returnedRentalId = rentalId
        return NetworkResult.Success(Unit)
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
