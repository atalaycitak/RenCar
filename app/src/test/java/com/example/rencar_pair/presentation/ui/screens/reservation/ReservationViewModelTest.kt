package com.example.rencar_pair.presentation.ui.screens.reservation

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.ReservationStatus
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
    fun `confirm reservation creates active reservation first`() = runTest {
        val reservationRepository = FakeReservationRepositoryForTest()
        val viewModel = createViewModel(reservationRepository = reservationRepository)

        advanceUntilIdle()
        viewModel.onIntent(ReservationIntent.ConfirmReservation)
        advanceUntilIdle()

        assertEquals("reservation-1", viewModel.state.value.activeReservation?.id)
        assertEquals("vehicle-1", reservationRepository.reservedVehicleId)
        assertEquals(null, viewModel.state.value.rentalId)
    }

    @Test
    fun `confirm reservation creates preparing rental when active reservation exists`() = runTest {
        val reservationRepository = FakeReservationRepositoryForTest(
            initialReservation = activeReservationForTest()
        )
        val rentalRepository = FakeRentalRepositoryForTest()
        val viewModel = createViewModel(
            reservationRepository = reservationRepository,
            rentalRepository = rentalRepository
        )

        advanceUntilIdle()
        viewModel.onIntent(ReservationIntent.ConfirmReservation)
        advanceUntilIdle()

        assertEquals("rental-1", viewModel.state.value.rentalId)
        assertEquals("vehicle-1", rentalRepository.createdVehicleId)
        assertEquals(com.example.rencar_pair.domain.model.RentalPlan.PerMinute, rentalRepository.createdPlan)
    }

    private fun createViewModel(
        reservationRepository: FakeReservationRepositoryForTest = FakeReservationRepositoryForTest(),
        rentalRepository: FakeRentalRepositoryForTest = FakeRentalRepositoryForTest()
    ): ReservationViewModel {
        val vehicleRepository = FakeVehicleRepositoryForTest()
        val savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "vehicle-1"))
        return ReservationViewModel(
            savedStateHandle = savedStateHandle,
            vehicleUseCases = VehicleUseCases(vehicleRepository),
            calculateReservationQuoteUseCase = CalculateReservationQuoteUseCase(),
            rentalUseCases = RentalUseCases(rentalRepository, reservationRepository)
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
    constructor() : this(null)

    constructor(initialReservation: Reservation?) {
        activeReservation = initialReservation
    }

    var reservedVehicleId: String? = null
    var createdVehicleId: String? = null
    var createdEndDate: String? = null
    var createdPlan: String? = null
    private var rental: Rental? = null
    private var activeReservation: Reservation? = null

    override suspend fun createReservation(vehicleId: String): NetworkResult<Reservation> {
        reservedVehicleId = vehicleId
        activeReservation = activeReservationForTest(vehicleId = vehicleId)
        return NetworkResult.Success(activeReservation!!)
    }

    override suspend fun getActiveReservation(): NetworkResult<Reservation?> {
        return NetworkResult.Success(activeReservation)
    }

    override suspend fun createRental(
        vehicleId: String,
        endDate: String?,
        plan: String?
    ): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun getRentals(): NetworkResult<List<Rental>> = NetworkResult.Error("Not implemented")

    override suspend fun getRental(id: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun returnRental(id: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")
}

private class FakeRentalRepositoryForTest : com.example.rencar_pair.domain.repository.RentalRepository {
    var createdVehicleId: String? = null
    var createdEndDate: String? = null
    var createdPlan: com.example.rencar_pair.domain.model.RentalPlan? = null

    override suspend fun createRental(
        vehicleId: String,
        plan: com.example.rencar_pair.domain.model.RentalPlan?,
        endDate: String?
    ): NetworkResult<Rental> {
        createdVehicleId = vehicleId
        createdPlan = plan
        createdEndDate = endDate
        return NetworkResult.Success(
            Rental(
                id = "rental-1",
                userId = "user-1",
                vehicleId = vehicleId,
                plan = plan ?: com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
                status = if (plan == com.example.rencar_pair.domain.model.RentalPlan.PerMinute) RentalStatus.Preparing else RentalStatus.Active,
                paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
                paymentMethod = null,
                totalPrice = 1230.0,
                startFee = 15.0,
                serviceFee = null,
                distanceKm = null,
                durationMinutes = null,
                discountAmount = 0.0,
                startedAt = Instant.parse("2026-01-01T10:00:00Z"),
                endedAt = null,
                scheduledEndDate = endDate?.let { Instant.parse(it) } ?: Instant.parse("2026-01-01T10:00:00Z"),
                createdAt = Instant.parse("2026-01-01T09:00:00Z")
            )
        )
    }

    override suspend fun getMyRentals(): NetworkResult<List<Rental>> = NetworkResult.Error("Not implemented")

    override suspend fun getRental(id: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

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

private fun activeReservationForTest(vehicleId: String = "vehicle-1"): Reservation {
    return Reservation(
        id = "reservation-1",
        userId = "user-1",
        vehicleId = vehicleId,
        status = ReservationStatus.Active,
        expiresAt = Instant.parse("2026-01-01T10:15:00Z"),
        remainingSeconds = 900,
        createdAt = Instant.parse("2026-01-01T10:00:00Z")
    )
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
