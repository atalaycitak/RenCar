package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.CalculateReservationQuoteUseCase
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `select days recalculates quote for quick duration options`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ReservationIntent.SelectDays(7))

        val state = viewModel.state.value
        assertEquals(7, state.selectedDays)
        assertEquals(7560.0, state.quote?.totalPrice ?: 0.0, 0.0)
    }

    @Test
    fun `confirm reservation creates rental after quote is ready`() = runTest {
        val reservationRepository = FakeReservationRepositoryForTest()
        val viewModel = createViewModel(reservationRepository = reservationRepository)

        advanceUntilIdle()
        val expectedEndDate = viewModel.state.value.quote?.endDateIso
        viewModel.onIntent(ReservationIntent.ConfirmReservation)
        advanceUntilIdle()

        assertEquals("rental-1", viewModel.state.value.rentalId)
        assertEquals("vehicle-1", reservationRepository.createdVehicleId)
        assertEquals(expectedEndDate, reservationRepository.createdEndDate)
    }

    private fun createViewModel(
        reservationRepository: FakeReservationRepositoryForTest = FakeReservationRepositoryForTest()
    ): ReservationViewModel {
        val vehicleRepository = FakeVehicleRepositoryForTest()
        val savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "vehicle-1"))
        return ReservationViewModel(
            savedStateHandle = savedStateHandle,
            vehicleUseCases = VehicleUseCases(vehicleRepository),
            calculateReservationQuoteUseCase = CalculateReservationQuoteUseCase(),
            rentalUseCases = RentalUseCases(reservationRepository)
        )
    }
}

private class FakeVehicleRepositoryForTest : VehicleRepository {
    private val vehicle = Vehicle(
        id = "vehicle-1",
        plate = "34 TST 001",
        brand = "Renault",
        model = "Clio",
        type = VehicleType.Hatchback,
        pricePerDay = 1000.0,
        status = VehicleStatus.Available,
        latitude = 41.0,
        longitude = 29.0,
        rangeKm = 320,
        locationName = "Test lokasyon"
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
        return NetworkResult.Success(vehicle)
    }
}

private class FakeReservationRepositoryForTest : ReservationRepository {
    var createdVehicleId: String? = null
    var createdEndDate: String? = null
    private var rental: Rental? = null

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        createdVehicleId = vehicleId
        createdEndDate = endDate
        rental = Rental(
            id = "rental-1",
            userId = "user-1",
            vehicleId = vehicleId,
            startDate = Instant.parse("2026-01-01T10:00:00Z"),
            endDate = Instant.parse(endDate),
            totalPrice = 1230.0,
            status = RentalStatus.Active
        )
        return NetworkResult.Success(rental!!)
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(listOfNotNull(rental))
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        return NetworkResult.Success(
            rental ?: Rental(
                id = id,
                userId = "user-1",
                vehicleId = "vehicle-1",
                startDate = Instant.parse("2026-01-01T10:00:00Z"),
                endDate = Instant.parse("2026-01-02T10:00:00Z"),
                totalPrice = 1230.0,
                status = RentalStatus.Active
            )
        )
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        val completed = (rental ?: Rental(
            id = id,
            userId = "user-1",
            vehicleId = "vehicle-1",
            startDate = Instant.parse("2026-01-01T10:00:00Z"),
            endDate = Instant.parse("2026-01-02T10:00:00Z"),
            totalPrice = 1230.0,
            status = RentalStatus.Active
        )).copy(status = RentalStatus.Completed)
        rental = completed
        return NetworkResult.Success(completed)
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
