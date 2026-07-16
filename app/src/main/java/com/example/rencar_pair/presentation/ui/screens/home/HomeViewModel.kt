package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import com.example.rencar_pair.presentation.mvi.NoEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch

class HomeViewModel(
    private val vehicleUseCases: VehicleUseCases,
    private val vehicleLocationRepository: VehicleLocationRepository,
    private val locationTracker: LocationTracker
) : BaseMviViewModel<HomeState, HomeIntent, NoEffect>(
    HomeState(vehicleLocationStreamMode = vehicleLocationRepository.streamMode)
) {

    private var locationUpdatesJob: Job? = null
    private var vehicleLocationUpdatesJob: Job? = null

    init {
        onIntent(HomeIntent.LoadVehicles)
        startVehicleLocationUpdates()
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadVehicles -> loadVehicles()
            is HomeIntent.SelectVehicle -> updateState {
                it.copy(selectedVehicleId = intent.id)
            }
            is HomeIntent.UpdateVehicleTypeFilter -> {
                updateState {
                    it.copy(
                        selectedVehicleType = intent.type,
                        selectedVehicleId = null,
                        errorMessage = null
                    )
                }
                loadVehicles(intent.type)
            }
            is HomeIntent.UpdateMaxPriceFilter -> updateFilterState {
                it.copy(maxDailyPrice = intent.maxPrice)
            }
            is HomeIntent.UpdateMinRangeFilter -> updateFilterState {
                it.copy(minRangeKm = intent.minRangeKm)
            }
            HomeIntent.ClearFilters -> {
                updateState {
                    it.copy(
                        selectedVehicleType = null,
                        maxDailyPrice = null,
                        minRangeKm = null,
                        selectedVehicleId = null,
                        errorMessage = null
                    )
                }
                loadVehicles(null)
            }
            is HomeIntent.LocationPermissionChanged -> {
                updateState {
                    it.copy(
                        locationPermissionGranted = intent.granted,
                        userLocation = if (intent.granted) it.userLocation else null
                    )
                }
                if (intent.granted) {
                    startLocationUpdates()
                } else {
                    stopLocationUpdates()
                }
            }
            HomeIntent.FetchUserLocation -> {
                if (currentState().locationPermissionGranted) {
                    fetchUserLocation()
                }
            }
            HomeIntent.FocusUserLocation -> {
                updateState { it.copy(selectedVehicleId = null) }
                if (currentState().locationPermissionGranted) {
                    fetchUserLocation()
                }
            }
        }
    }

    private fun loadVehicles(type: VehicleType? = currentState().selectedVehicleType) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getAvailableVehicles(type = type?.toApiQuery(), includeBusy = true)) {
                is NetworkResult.Success -> updateState {
                    val next = it.copy(
                        vehicles = result.data,
                        isLoading = false
                    )
                    if (next.filteredVehicles.any { vehicle -> vehicle.id == next.selectedVehicleId }) {
                        next
                    } else {
                        next.copy(selectedVehicleId = null)
                    }
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun fetchUserLocation() {
        launchCoroutine {
            val location = locationTracker.getCurrentLocation()
            updateState { it.copy(userLocation = location) }
        }
    }

    private fun startLocationUpdates() {
        if (locationUpdatesJob?.isActive == true) return

        locationUpdatesJob = launchCoroutine {
            locationTracker.observeLocationUpdates()
                .catch {
                    updateState { state -> state.copy(userLocation = null) }
                }
                .collect { location ->
                    updateState { it.copy(userLocation = location) }
                }
        }
    }

    private fun stopLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    private fun startVehicleLocationUpdates() {
        if (vehicleLocationUpdatesJob?.isActive == true) return

        vehicleLocationUpdatesJob = launchCoroutine {
            vehicleLocationRepository.observeVehiclePositions()
                .catch { }
                .collect { positions ->
                    applyVehiclePositions(positions)
                }
        }
    }

    private fun applyVehiclePositions(positions: List<VehiclePosition>) {
        if (positions.isEmpty()) return
        val positionsById = positions.associateBy { it.vehicleId }
        updateState { state ->
            state.copy(
                vehicles = state.vehicles.map { vehicle ->
                    val position = positionsById[vehicle.id] ?: return@map vehicle
                    vehicle.copy(
                        latitude = position.latitude,
                        longitude = position.longitude,
                        status = position.status,
                        locationUpdatedAt = position.updatedAt ?: vehicle.locationUpdatedAt,
                        canReserve = vehicle.canReserve && position.status == VehicleStatus.Available
                    )
                },
                hasLiveVehicleUpdates = true
            )
        }
    }

    private fun updateFilterState(transform: (HomeState) -> HomeState) {
        updateState { current ->
            val next = transform(current).copy(errorMessage = null)
            val selectedStillVisible = next.filteredVehicles.any { it.id == next.selectedVehicleId }
            if (selectedStillVisible) {
                next
            } else {
                next.copy(selectedVehicleId = next.firstVisibleVehicleId())
            }
        }
    }

    private fun HomeState.firstVisibleVehicleId(): String? = filteredVehicles.firstOrNull()?.id

    private fun VehicleType.toApiQuery(): String = when (this) {
        VehicleType.Sedan -> "SEDAN"
        VehicleType.Suv -> "SUV"
        VehicleType.Hatchback -> "HATCHBACK"
        VehicleType.Station -> "STATION"
        VehicleType.Minivan -> "MINIVAN"
        VehicleType.Unknown -> "UNKNOWN"
    }
}
