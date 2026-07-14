package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.repository.VehicleRepository
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
    fun `type filter reloads vehicles with api query`() = runTest {
        val repository = FakeVehicleRepositoryForHomeTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.UpdateVehicleTypeFilter(VehicleType.Suv))
        advanceUntilIdle()

        assertEquals("SUV", repository.lastTypeQuery)
        assertEquals(1, viewModel.state.value.filteredVehicles.size)
        assertEquals(VehicleType.Suv, viewModel.state.value.filteredVehicles.first().type)
    }

    @Test
    fun `clear filters reloads all vehicles`() = runTest {
        val repository = FakeVehicleRepositoryForHomeTest()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.UpdateVehicleTypeFilter(VehicleType.Suv))
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.ClearFilters)
        advanceUntilIdle()

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
                    status = VehicleStatus.Rented
                )
            )
        )
        advanceUntilIdle()

        val updated = viewModel.state.value.vehicles.first { it.id == "sedan-1" }
        assertEquals(41.5, updated.latitude, 0.0001)
        assertEquals(29.5, updated.longitude, 0.0001)
        assertEquals(VehicleStatus.Rented, updated.status)
        assertEquals(false, updated.canReserve)
    }

    private fun createViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepositoryForHomeTest(),
        vehicleLocationRepository: VehicleLocationRepository = EmptyVehicleLocationRepositoryForHomeTest(),
        locationTracker: LocationTracker = FakeLocationTrackerForHomeTest()
    ): HomeViewModel {
        return HomeViewModel(
            vehicleUseCases = VehicleUseCases(vehicleRepository),
            vehicleLocationRepository = vehicleLocationRepository,
            locationTracker = locationTracker
        )
    }
}

private class FakeVehicleRepositoryForHomeTest : VehicleRepository {
    var lastTypeQuery: String? = null

    private val vehicles = listOf(
        testVehicle("sedan-1", VehicleType.Sedan, 900.0, 420),
        testVehicle("sedan-2", VehicleType.Sedan, 1400.0, 350, latitude = 41.03, longitude = 29.03),
        testVehicle("suv-1", VehicleType.Suv, 2200.0, 410, latitude = 41.06, longitude = 29.06),
        testVehicle("hatch-1", VehicleType.Hatchback, 1800.0, 260, latitude = 41.09, longitude = 29.09)
    )

    override suspend fun getAvailableVehicles(
        type: String?,
        page: Int?,
        limit: Int?
    ): NetworkResult<List<Vehicle>> {
        lastTypeQuery = type
        val filtered = type?.let { query ->
            vehicles.filter { it.type.name.equals(query, ignoreCase = true) }
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
    longitude: Double = 29.0
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
        rangeKm = rangeKm
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
