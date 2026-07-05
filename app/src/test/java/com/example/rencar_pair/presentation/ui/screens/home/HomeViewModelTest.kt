package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleRepository
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

    private fun createViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepositoryForHomeTest()
    ): HomeViewModel {
        return HomeViewModel(
            vehicleUseCases = VehicleUseCases(vehicleRepository),
            locationTracker = FakeLocationTrackerForHomeTest()
        )
    }
}

private class FakeVehicleRepositoryForHomeTest : VehicleRepository {
    var lastTypeQuery: String? = null

    private val vehicles = listOf(
        testVehicle("sedan-1", VehicleType.Sedan, 900.0, 420),
        testVehicle("sedan-2", VehicleType.Sedan, 1400.0, 350),
        testVehicle("suv-1", VehicleType.Suv, 2200.0, 410),
        testVehicle("hatch-1", VehicleType.Hatchback, 1800.0, 260)
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
    override suspend fun getCurrentLocation(): UserLocation {
        return UserLocation(latitude = 41.0, longitude = 29.0)
    }
}

private fun testVehicle(
    id: String,
    type: VehicleType,
    price: Double,
    rangeKm: Int
): Vehicle {
    return Vehicle(
        id = id,
        plate = "34 TST $id",
        brand = "Test",
        model = id,
        type = type,
        pricePerDay = price,
        status = VehicleStatus.Available,
        latitude = 41.0,
        longitude = 29.0,
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
