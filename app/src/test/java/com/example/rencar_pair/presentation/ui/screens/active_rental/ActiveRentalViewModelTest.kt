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
            rentalUseCases = RentalUseCases(FakeReservationRepositoryForActiveRentalTest()),
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
            rentalUseCases = RentalUseCases(FakeReservationRepositoryForActiveRentalTest()),
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
        startDate = Instant.now().minusSeconds(3600),
        endDate = Instant.now().plusSeconds(86400),
        totalPrice = 1200.0,
        status = RentalStatus.Active
    )

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        return NetworkResult.Success(rental)
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(listOf(rental))
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        return NetworkResult.Success(rental.copy(id = id))
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        return NetworkResult.Success(rental.copy(id = id, status = RentalStatus.Completed))
    }
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
