package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.ReservationStatus
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.ReservationRepository
import com.example.rencar_pair.domain.repository.RentalRepository
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.repository.VehicleLocationStreamMode
import com.example.rencar_pair.domain.repository.VehicleRepository
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = HomeMainDispatcherRule()

    @Test
    fun `price filter narrows visible vehicles client side`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.UpdateMaxPriceFilter(1500))

        assertEquals(2, viewModel.state.value.filteredVehicles.size)
        assertTrue(viewModel.state.value.filteredVehicles.all { it.pricePerDay <= 1500.0 })
    }

    @Test
    fun `range filter narrows visible vehicles client side`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.UpdateMinRangeFilter(400))

        assertEquals(2, viewModel.state.value.filteredVehicles.size)
        assertTrue(viewModel.state.value.filteredVehicles.all { it.rangeKm >= 400 })
    }

    @Test
    fun `nearby vehicles are sorted by user location`() = runTest {
        val locationTracker = FakeLocationTrackerForHomeTest()
        val viewModel = createViewModel(locationTracker = locationTracker)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.LocationPermissionChanged(true))
        locationTracker.emitLocation(UserLocation(latitude = 41.0, longitude = 29.0))
        advanceUntilIdle()

        assertEquals("sedan-1", viewModel.state.value.nearbyVehicles.first().id)
    }

    @Test
    fun `type filter narrows vehicles client side by ui segment`() = runTest {
        val repository = FakeVehicleRepositoryForHomeTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val initialQueryCount = repository.queryCount

        viewModel.onIntent(HomeIntent.UpdateVehicleTypeFilter(VehicleType.Sedan))
        advanceUntilIdle()

        assertEquals(initialQueryCount, repository.queryCount)
        assertEquals(null, repository.lastTypeQuery)
        assertEquals(listOf("sedan-1", "sedan-2"), viewModel.state.value.filteredVehicles.map { it.id })
    }

    @Test
    fun `clear filters restores cached vehicles without reload`() = runTest {
        val repository = FakeVehicleRepositoryForHomeTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val initialQueryCount = repository.queryCount

        viewModel.onIntent(HomeIntent.UpdateVehicleTypeFilter(VehicleType.Suv))
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.ClearFilters)
        advanceUntilIdle()

        assertEquals(initialQueryCount, repository.queryCount)
        assertEquals(null, repository.lastTypeQuery)
        assertEquals(4, viewModel.state.value.filteredVehicles.size)
        assertTrue(!viewModel.state.value.hasActiveFilters)
    }

    @Test
    fun `location permission granted observes user location updates`() = runTest {
        val locationTracker = FakeLocationTrackerForHomeTest()
        val viewModel = createViewModel(locationTracker = locationTracker)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.LocationPermissionChanged(true))
        locationTracker.emitLocation(UserLocation(latitude = 41.0, longitude = 29.0))
        advanceUntilIdle()

        assertEquals(UserLocation(latitude = 41.0, longitude = 29.0), viewModel.state.value.userLocation)
        assertEquals(1, locationTracker.observeCount)
    }

    @Test
    fun `fetch user location is ignored until permission is granted`() = runTest {
        val locationTracker = FakeLocationTrackerForHomeTest()
        val viewModel = createViewModel(locationTracker = locationTracker)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.FetchUserLocation)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.userLocation)
        assertEquals(0, locationTracker.fetchCount)
    }

    @Test
    fun `focus user location clears selected vehicle`() = runTest {
        val locationTracker = FakeLocationTrackerForHomeTest()
        val viewModel = createViewModel(locationTracker = locationTracker)
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.SelectVehicle("suv-1"))

        viewModel.onIntent(HomeIntent.FocusUserLocation)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.selectedVehicleId)
    }

    @Test
    fun `location permission denied clears observed location`() = runTest {
        val locationTracker = FakeLocationTrackerForHomeTest()
        val viewModel = createViewModel(locationTracker = locationTracker)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.LocationPermissionChanged(true))
        locationTracker.emitLocation(UserLocation(latitude = 41.0, longitude = 29.0))
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.LocationPermissionChanged(false))
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.userLocation)
    }

    @Test
    fun `vehicle location stream updates map vehicle coordinates`() = runTest {
        val vehicleLocationRepository = FakeVehicleLocationRepositoryForHomeTest()
        val viewModel = createViewModel(vehicleLocationRepository = vehicleLocationRepository)
        advanceUntilIdle()

        vehicleLocationRepository.emitPositions(
            listOf(
                VehiclePosition(
                    vehicleId = "sedan-1",
                    latitude = 41.5,
                    longitude = 29.5,
                    status = VehicleStatus.Rented,
                    updatedAt = "2026-07-14T11:35:00Z"
                )
            )
        )
        advanceUntilIdle()

        val updated = viewModel.state.value.vehicles.first { it.id == "sedan-1" }
        assertEquals(41.5, updated.latitude, 0.0001)
        assertEquals(29.5, updated.longitude, 0.0001)
        assertEquals(VehicleStatus.Rented, updated.status)
        assertEquals(false, updated.canReserve)
        assertEquals("2026-07-14T11:35:00Z", updated.locationUpdatedAt)
        assertEquals(true, viewModel.state.value.hasLiveVehicleUpdates)
        assertEquals(VehicleLocationStreamMode.WebSocket, viewModel.state.value.vehicleLocationStreamMode)
    }

    @Test
    fun `active reservation enables unlock only for reserved vehicle`() = runTest {
        val vehicleLocationRepository = FakeVehicleLocationRepositoryForHomeTest()
        val viewModel = createViewModel(
            reservationRepository = FakeReservationRepositoryForHomeTest(
                activeReservation = Reservation(
                    id = "reservation-1",
                    userId = "user-1",
                    vehicleId = "sedan-1",
                    status = ReservationStatus.Active,
                    expiresAt = Instant.parse("2026-07-16T19:15:00Z"),
                    remainingSeconds = 840,
                    createdAt = Instant.parse("2026-07-16T19:00:00Z")
                )
            ),
            vehicleLocationRepository = vehicleLocationRepository
        )
        advanceUntilIdle()

        val reserved = viewModel.state.value.vehicles.first { it.id == "sedan-1" }
        val other = viewModel.state.value.vehicles.first { it.id == "suv-1" }

        assertEquals(VehicleStatus.Reserved, reserved.status)
        assertEquals(true, reserved.canUnlock)
        assertEquals(false, reserved.canReserve)
        assertEquals(false, other.canUnlock)
        assertEquals(false, other.canReserve)
        assertEquals(listOf("sedan-1"), viewModel.state.value.visibleVehicles.map { it.id })
        assertEquals("sedan-1", viewModel.state.value.highlightedVehicle?.id)
        assertEquals("sedan-1", vehicleLocationRepository.capturedActiveVehicleId)
    }

    @Test
    fun `active rental is loaded and published to vehicle location repository`() = runTest {
        val vehicleLocationRepository = FakeVehicleLocationRepositoryForHomeTest()
        val viewModel = createViewModel(
            rentalRepository = FakeRentalRepositoryForHomeTest(
                activeRental = ActiveRental(
                    id = "active-rental-1",
                    userId = "user-1",
                    vehicleId = "suv-1",
                    plan = RentalPlan.PerMinute,
                    status = RentalStatus.Active,
                    elapsedSeconds = 120.0,
                    currentCost = 28.0,
                    startedAt = Instant.parse("2026-07-18T10:00:00Z"),
                    distanceKm = 1.1,
                    durationMinutes = 2.0,
                    startFee = 15.0,
                    createdAt = Instant.parse("2026-07-18T10:00:00Z")
                )
            ),
            vehicleLocationRepository = vehicleLocationRepository
        )
        advanceUntilIdle()

        assertEquals("active-rental-1", viewModel.state.value.activeRental?.id)
        assertEquals("suv-1", vehicleLocationRepository.capturedActiveVehicleId)
        assertEquals("sedan-1", viewModel.state.value.highlightedVehicle?.id)
    }

    @Test
    fun `latest preparing rental is exposed so delivery checklist can be resumed`() = runTest {
        val viewModel = createViewModel(
            rentalRepository = FakeRentalRepositoryForHomeTest(
                rentals = listOf(
                    testRentalForHomeTest(
                        id = "old-preparing",
                        vehicleId = "sedan-1",
                        createdAt = Instant.parse("2026-07-18T09:00:00Z")
                    ),
                    testRentalForHomeTest(
                        id = "latest-preparing",
                        vehicleId = "suv-1",
                        createdAt = Instant.parse("2026-07-18T10:00:00Z")
                    )
                )
            )
        )
        advanceUntilIdle()

        assertEquals("latest-preparing", viewModel.state.value.pendingRental?.id)
        assertEquals("suv-1", viewModel.state.value.highlightedVehicle?.id)
    }

    private fun createViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepositoryForHomeTest(),
        rentalRepository: RentalRepository = FakeRentalRepositoryForHomeTest(),
        reservationRepository: ReservationRepository = FakeReservationRepositoryForHomeTest(),
        vehicleLocationRepository: VehicleLocationRepository = EmptyVehicleLocationRepositoryForHomeTest(),
        locationTracker: LocationTracker = FakeLocationTrackerForHomeTest()
    ): HomeViewModel {
        return HomeViewModel(
            vehicleUseCases = VehicleUseCases(vehicleRepository),
            rentalUseCases = RentalUseCases(rentalRepository, reservationRepository),
            vehicleLocationRepository = vehicleLocationRepository,
            locationTracker = locationTracker
        )
    }
}

private class FakeReservationRepositoryForHomeTest(
    private val activeReservation: Reservation? = null
) : ReservationRepository {
    override suspend fun createRental(
        vehicleId: String,
        endDate: String?,
        plan: String?
    ): NetworkResult<Rental> {
        return NetworkResult.Success(
            Rental(
                id = "rental-1",
                userId = "user-1",
                vehicleId = vehicleId,
                plan = com.example.rencar_pair.domain.model.RentalPlan.PerMinute,
                status = RentalStatus.Preparing,
                paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
                paymentMethod = null,
                totalPrice = 0.0,
                startFee = 15.0,
                serviceFee = null,
                distanceKm = null,
                durationMinutes = null,
                discountAmount = 0.0,
                startedAt = Instant.parse("2026-07-16T19:00:00Z"),
                endedAt = null,
                scheduledEndDate = Instant.parse("2026-07-16T19:00:00Z"),
                createdAt = Instant.parse("2026-07-16T19:00:00Z")
            )
        )
    }

    override suspend fun getActiveReservation(): NetworkResult<Reservation?> {
        return NetworkResult.Success(activeReservation)
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(emptyList())
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun returnRental(rentalId: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun createReservation(vehicleId: String): NetworkResult<Reservation> = NetworkResult.Error("Not implemented")

    override suspend fun cancelReservation(id: String): NetworkResult<Unit> = NetworkResult.Error("Not implemented")
}

private class FakeRentalRepositoryForHomeTest(
    private val activeRental: ActiveRental? = null,
    private val rentals: List<Rental> = emptyList()
) : RentalRepository {
    override suspend fun createRental(
        vehicleId: String,
        plan: com.example.rencar_pair.domain.model.RentalPlan?,
        endDate: String?
    ): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun getMyRentals(): NetworkResult<List<Rental>> = NetworkResult.Success(rentals)

    override suspend fun getRental(id: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun getActiveRental(): NetworkResult<ActiveRental?> = NetworkResult.Success(activeRental)

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<com.example.rencar_pair.domain.model.RentalPhotosState> = NetworkResult.Error("Not implemented")

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: com.example.rencar_pair.domain.model.RentalPhotoSide,
        photoUri: String
    ): NetworkResult<com.example.rencar_pair.domain.model.RentalPhotosState> = NetworkResult.Error("Not implemented")

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> = NetworkResult.Error("Not implemented")

    override suspend fun finishRental(rentalId: String): NetworkResult<com.example.rencar_pair.domain.model.FinishedRental> = NetworkResult.Error("Not implemented")

    override suspend fun returnRental(rentalId: String): NetworkResult<Rental> = NetworkResult.Error("Not implemented")

    override suspend fun payRental(
        rentalId: String,
        method: com.example.rencar_pair.domain.model.PaymentMethod,
        cardId: String?,
        discountCode: String?
    ): NetworkResult<Unit> = NetworkResult.Error("Not implemented")

    override suspend fun cancelRental(rentalId: String): NetworkResult<Unit> = NetworkResult.Error("Not implemented")
}

private class FakeVehicleRepositoryForHomeTest : VehicleRepository {
    var lastTypeQuery: String? = null
    var queryCount: Int = 0

    private val vehicles = listOf(
        testVehicle("sedan-1", VehicleType.Unknown, 900.0, 420, segment = "COMFORT"),
        testVehicle("sedan-2", VehicleType.Sedan, 1400.0, 350, latitude = 41.03, longitude = 29.03),
        testVehicle("suv-1", VehicleType.Unknown, 2200.0, 410, latitude = 41.06, longitude = 29.06, segment = "SUV"),
        testVehicle("hatch-1", VehicleType.Unknown, 1800.0, 260, latitude = 41.09, longitude = 29.09, segment = "ECONOMY")
    )

    override suspend fun getAvailableVehicles(
        type: String?,
        page: Int?,
        limit: Int?,
        includeBusy: Boolean
    ): NetworkResult<List<Vehicle>> {
        queryCount += 1
        lastTypeQuery = type
        val filtered = type?.let { query ->
            vehicles.filter {
                it.type.name.equals(query, ignoreCase = true) ||
                    it.segment.equals(query, ignoreCase = true)
            }
        } ?: vehicles
        return NetworkResult.Success(filtered)
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return NetworkResult.Success(vehicles.first { it.id == id })
    }
}

private class FakeLocationTrackerForHomeTest : LocationTracker {
    var fetchCount: Int = 0
    var observeCount: Int = 0
    private val locationUpdates = MutableSharedFlow<UserLocation>(replay = 1)

    override suspend fun getCurrentLocation(): UserLocation {
        fetchCount += 1
        return UserLocation(latitude = 41.0, longitude = 29.0)
    }

    override fun observeLocationUpdates(): Flow<UserLocation> {
        observeCount += 1
        return locationUpdates
    }

    suspend fun emitLocation(location: UserLocation) {
        locationUpdates.emit(location)
    }
}

private class EmptyVehicleLocationRepositoryForHomeTest : VehicleLocationRepository {
    override fun observeVehiclePositions(): Flow<List<VehiclePosition>> = emptyFlow()
}

private class FakeVehicleLocationRepositoryForHomeTest : VehicleLocationRepository {
    private val positions = MutableSharedFlow<List<VehiclePosition>>(replay = 1)
    var capturedActiveVehicleId: String? = null

    override val streamMode: VehicleLocationStreamMode = VehicleLocationStreamMode.WebSocket

    override fun setActiveVehicleId(vehicleId: String?) {
        capturedActiveVehicleId = vehicleId
    }

    override fun observeVehiclePositions(): Flow<List<VehiclePosition>> = positions

    suspend fun emitPositions(next: List<VehiclePosition>) {
        positions.emit(next)
    }
}

private fun testVehicle(
    id: String,
    type: VehicleType,
    price: Double,
    rangeKm: Int,
    latitude: Double = 41.0,
    longitude: Double = 29.0,
    segment: String? = null
): Vehicle {
    return Vehicle(
        id = id,
        plate = "34 TST $id",
        brand = "Test",
        model = id,
        type = type,
        pricePerDay = price,
        status = VehicleStatus.Available,
        latitude = latitude,
        longitude = longitude,
        rangeKm = rangeKm,
        segment = segment
    )
}

private fun testRentalForHomeTest(
    id: String,
    vehicleId: String,
    createdAt: Instant
): Rental {
    return Rental(
        id = id,
        userId = "user-1",
        vehicleId = vehicleId,
        plan = RentalPlan.PerMinute,
        status = RentalStatus.Preparing,
        paymentStatus = com.example.rencar_pair.domain.model.PaymentStatus.Unpaid,
        paymentMethod = null,
        totalPrice = 0.0,
        startFee = 15.0,
        serviceFee = null,
        distanceKm = null,
        durationMinutes = null,
        discountAmount = 0.0,
        startedAt = null,
        endedAt = null,
        scheduledEndDate = null,
        createdAt = createdAt
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
