package com.example.rencar_pair.presentation.ui.screens.active_rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveRentalViewModelTest {

    @get:Rule
    val mainDispatcherRule = ActiveRentalMainDispatcherRule()

    @Test
    fun `load rental also loads active vehicle detail`() = runTest {
        val viewModel = ActiveRentalViewModel(
            rentalUseCases = RentalUseCases(FakeRentalRepositoryForActiveRentalTest(), FakeReservationRepositoryForActiveRentalTest()),
            vehicleUseCases = VehicleUseCases(FakeVehicleRepositoryForActiveRentalTest())
        )
        viewModel.setTimerEnabled(false)

        viewModel.onIntent(ActiveRentalIntent.LoadRental("rental-1"))
        runCurrent()
        runCurrent()

        assertEquals("rental-1", viewModel.state.value.rental?.id)
        assertEquals("vehicle-1", viewModel.state.value.vehicle?.id)
        assertNotNull(viewModel.state.value.vehicle)
    }

    @Test
    fun `finish rental emits return navigation when rental is loaded`() = runTest {
        val viewModel = ActiveRentalViewModel(
            rentalUseCases = RentalUseCases(FakeRentalRepositoryForActiveRentalTest(), FakeReservationRepositoryForActiveRentalTest()),
            vehicleUseCases = VehicleUseCases(FakeVehicleRepositoryForActiveRentalTest())
        )
        viewModel.setTimerEnabled(false)

        viewModel.onIntent(ActiveRentalIntent.LoadRental("rental-1"))
        runCurrent()
        runCurrent()

        val effect = async { viewModel.effect.first() }
        viewModel.onIntent(ActiveRentalIntent.FinishRental)
        runCurrent()

        assertEquals("rental-1", viewModel.state.value.rental?.id)
        assertEquals(ActiveRentalEffect.NavigateToReturnVehicle("rental-1"), effect.await())
    }
}

private class FakeReservationRepositoryForActiveRentalTest : ReservationRepository {
    private val rental = Rental(
        id = "rental-1",
        userId = "user-1",
        vehicleId = "vehicle-1",
        plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
        status = RentalStatus.Active,
        paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
        paymentMethod = null,
        totalPrice = 1200.0,
        startFee = 15.0,
        serviceFee = null,
        distanceKm = null,
        durationMinutes = null,
        discountAmount = 0.0,
        startedAt = Instant.now().minusSeconds(3600),
        endedAt = null,
        scheduledEndDate = Instant.now().plusSeconds(86400),
        createdAt = Instant.now().minusSeconds(3600)
    )

    override suspend fun createRental(
        vehicleId: String,
        endDate: String?,
        plan: String?
    ): NetworkResult<Rental> {
        return NetworkResult.Success(rental)
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(listOf(rental))
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        return NetworkResult.Success(rental.copy(id = id))
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        return NetworkResult.Success(rental.copy(status = RentalStatus.Completed))
    }
}

private class FakeRentalRepositoryForActiveRentalTest : com.example.rencar_pair.domain.repository.RentalRepository {
    private val rental = Rental(
        id = "rental-1",
        userId = "user-1",
        vehicleId = "vehicle-1",
        plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
        status = RentalStatus.Active,
        paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
        paymentMethod = null,
        totalPrice = 1200.0,
        startFee = 15.0,
        serviceFee = null,
        distanceKm = null,
        durationMinutes = null,
        discountAmount = 0.0,
        startedAt = Instant.now().minusSeconds(3600),
        endedAt = null,
        scheduledEndDate = Instant.now().plusSeconds(86400),
        createdAt = Instant.now().minusSeconds(3600)
    )

    override suspend fun createRental(
        vehicleId: String,
        plan: com.example.rencar_pair.domain.model.RentalPlan?,
        endDate: String?
    ): NetworkResult<Rental> = NetworkResult.Success(rental)

    override suspend fun getMyRentals(): NetworkResult<List<Rental>> = NetworkResult.Success(listOf(rental))

    override suspend fun getRental(id: String): NetworkResult<Rental> = NetworkResult.Success(rental.copy(id = id))

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

    override suspend fun cancelRental(rentalId: String): NetworkResult<Unit> = NetworkResult.Error("Not implemented")
}

private class FakeVehicleRepositoryForActiveRentalTest : VehicleRepository {
    private val vehicle = Vehicle(
        id = "vehicle-1",
        plate = "34 ACT 001",
        brand = "Renault",
        model = "Clio",
        type = VehicleType.Hatchback,
        pricePerDay = 1200.0,
        status = VehicleStatus.Available,
        latitude = 41.0,
        longitude = 29.0,
        rangeKm = 420,
        locationName = "Test teslim noktasi"
    )

    override suspend fun getAvailableVehicles(
        type: String?,
        page: Int?,
        limit: Int?,
        includeBusy: Boolean
    ): NetworkResult<List<Vehicle>> {
        return NetworkResult.Success(listOf(vehicle))
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return NetworkResult.Success(vehicle.copy(id = id))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveRentalMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
