package com.example.rencar_pair.presentation.ui.screens.reservation

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.CreateRentalUseCase
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ReservationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads vehicle and recalculates quote when days change`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onIntent(ReservationIntent.IncreaseDays)

        val state = viewModel.state.value
        assertEquals("Renault Clio", state.vehicle?.title)
        assertEquals(2, state.selectedDays)
        assertEquals(2310.0, state.quote?.totalPrice ?: 0.0, 0.0)
    }

    @Test
    fun `confirm reservation creates rental after quote is ready`() = runTest {
        val reservationRepository = FakeReservationRepositoryForTest()
        val viewModel = createViewModel(reservationRepository = reservationRepository)

        advanceUntilIdle()
        viewModel.onIntent(ReservationIntent.ConfirmReservation)
        advanceUntilIdle()

        assertEquals("rental-1", viewModel.state.value.rentalId)
        assertEquals("vehicle-1", reservationRepository.createdVehicleId)
    }

    private fun createViewModel(
        reservationRepository: FakeReservationRepositoryForTest = FakeReservationRepositoryForTest()
    ): ReservationViewModel {
        val vehicleRepository = FakeVehicleRepositoryForTest()
        return ReservationViewModel(
            vehicleId = "vehicle-1",
            getVehicleDetailUseCase = GetVehicleDetailUseCase(vehicleRepository),
            calculateReservationQuoteUseCase = CalculateReservationQuoteUseCase(),
            createRentalUseCase = CreateRentalUseCase(reservationRepository)
        )
    }
}

private class FakeVehicleRepositoryForTest : VehicleRepository {
    private val vehicle = Vehicle(
        id = "vehicle-1",
        plate = "34 TST 001",
        brand = "Renault",
        model = "Clio",
        type = "HATCHBACK",
        pricePerDay = 1000.0,
        status = "AVAILABLE",
        latitude = 41.0,
        longitude = 29.0,
        rangeKm = 320,
        locationName = "Test lokasyon"
    )

    override suspend fun getAvailableVehicles(): NetworkResult<List<Vehicle>> {
        return NetworkResult.Success(listOf(vehicle))
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return NetworkResult.Success(vehicle)
    }
}

private class FakeReservationRepositoryForTest : ReservationRepository {
    var createdVehicleId: String? = null

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        createdVehicleId = vehicleId
        return NetworkResult.Success(
            Rental(
                id = "rental-1",
                vehicleId = vehicleId,
                startDate = "now",
                endDate = endDate,
                totalPrice = 1230.0,
                status = "ACTIVE"
            )
        )
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
