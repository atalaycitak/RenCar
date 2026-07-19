package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.usecase.RentalUseCases
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.domain.usecase.vehicle.FilterHomeVehiclesUseCase
import com.example.rencar_pair.domain.usecase.vehicle.HomeVehicleFilterParams
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import com.example.rencar_pair.presentation.mvi.NoEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch

class HomeViewModel(
    private val vehicleUseCases: VehicleUseCases,
    private val rentalUseCases: RentalUseCases,
    private val vehicleLocationRepository: VehicleLocationRepository,
    private val locationTracker: LocationTracker,
    private val filterHomeVehiclesUseCase: FilterHomeVehiclesUseCase
) : BaseMviViewModel<HomeState, HomeIntent, NoEffect>(
    HomeState(vehicleLocationStreamMode = vehicleLocationRepository.streamMode)
) {

    private var locationUpdatesJob: Job? = null
    private var vehicleLocationUpdatesJob: Job? = null

    init {
        onIntent(HomeIntent.LoadVehicles)
        onIntent(HomeIntent.LoadActiveReservation)
        onIntent(HomeIntent.LoadActiveRental)
        onIntent(HomeIntent.LoadPendingRental)
        startVehicleLocationUpdates()
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadVehicles -> loadVehicles()
            HomeIntent.LoadActiveReservation -> loadActiveReservation()
            HomeIntent.LoadActiveRental -> loadActiveRental()
            HomeIntent.LoadPendingRental -> loadPendingRental()
            is HomeIntent.SelectVehicle -> updateComputedState {
                it.copy(selectedVehicleId = intent.id)
            }
            is HomeIntent.UpdateVehicleTypeFilter -> {
                updateComputedState {
                    it.copy(
                        selectedVehicleType = intent.type,
                        selectedVehicleId = null,
                        errorMessage = null
                    )
                }
            }
            is HomeIntent.UpdateMaxPriceFilter -> updateFilterState {
                it.copy(maxDailyPrice = intent.maxPrice)
            }
            is HomeIntent.UpdateMinRangeFilter -> updateFilterState {
                it.copy(minRangeKm = intent.minRangeKm)
            }
            HomeIntent.ClearFilters -> {
                updateComputedState {
                    it.copy(
                        selectedVehicleType = null,
                        maxDailyPrice = null,
                        minRangeKm = null,
                        selectedVehicleId = null,
                        errorMessage = null
                    )
                }
            }
            is HomeIntent.LocationPermissionChanged -> {
                updateComputedState {
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
                updateComputedState { it.copy(selectedVehicleId = null) }
                if (currentState().locationPermissionGranted) {
                    fetchUserLocation()
                }
            }
        }
    }

    private fun loadVehicles() {
        launchCoroutine {
            updateComputedState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getAvailableVehicles(type = null, includeBusy = true)) {
                is NetworkResult.Success -> updateComputedState {
                    val next = it.copy(
                        vehicles = result.data.withReservationState(it.activeReservation),
                        isLoading = false
                    )
                    next
                }
                is NetworkResult.Error -> updateComputedState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun fetchUserLocation() {
        launchCoroutine {
            val location = locationTracker.getCurrentLocation()
            updateComputedState { it.copy(userLocation = location) }
        }
    }

    private fun loadActiveReservation() {
        launchCoroutine {
            when (val result = rentalUseCases.getActiveReservation()) {
                is NetworkResult.Success -> {
                    if (currentState().activeRental == null) {
                        vehicleLocationRepository.setActiveVehicleId(result.data?.vehicleId)
                    }
                    updateComputedState { state ->
                        state.copy(
                            activeReservation = result.data,
                            vehicles = state.vehicles.withReservationState(result.data)
                        )
                    }
                }
                is NetworkResult.Error -> {
                    if (currentState().activeRental == null) {
                        vehicleLocationRepository.setActiveVehicleId(null)
                    }
                    updateComputedState { state ->
                        state.copy(activeReservation = null)
                    }
                }
            }
        }
    }

    private fun loadActiveRental() {
        launchCoroutine {
            when (val result = rentalUseCases.getActiveRental()) {
                is NetworkResult.Success -> {
                    vehicleLocationRepository.setActiveVehicleId(
                        result.data?.vehicleId ?: currentState().activeReservation?.vehicleId
                    )
                    updateComputedState { state ->
                        state.copy(activeRental = result.data)
                    }
                }
                is NetworkResult.Error -> {
                    if (currentState().activeReservation == null) {
                        vehicleLocationRepository.setActiveVehicleId(null)
                    }
                    updateComputedState { state ->
                        state.copy(activeRental = null)
                    }
                }
            }
        }
    }

    private fun loadPendingRental() {
        launchCoroutine {
            when (val result = rentalUseCases.getMyRentals()) {
                is NetworkResult.Success -> updateComputedState { state ->
                    state.copy(pendingRental = result.data.latestPreparingRental())
                }
                is NetworkResult.Error -> updateComputedState { state ->
                    state.copy(pendingRental = null)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (locationUpdatesJob?.isActive == true) return

        locationUpdatesJob = launchCoroutine {
            locationTracker.observeLocationUpdates()
                .catch {
                    updateComputedState { state -> state.copy(userLocation = null) }
                }
                .collect { location ->
                    updateComputedState { it.copy(userLocation = location) }
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
        updateComputedState { state ->
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
                }.withReservationState(state.activeReservation),
                hasLiveVehicleUpdates = true
            )
        }
    }

    private fun updateFilterState(transform: (HomeState) -> HomeState) {
        updateComputedState { current ->
            val next = transform(current).copy(errorMessage = null)
            val selectedStillVisible = next.filteredVehicles.any { it.id == next.selectedVehicleId }
            if (selectedStillVisible) {
                next
            } else {
                next.copy(selectedVehicleId = next.filteredVehicles.firstOrNull()?.id)
            }
        }
    }

    private fun updateComputedState(transform: (HomeState) -> HomeState) {
        updateState { current ->
            var next = transform(current)
            
            // Re-apply filter logic dynamically to ensure state lists are always up to date
            // before we push the state down to the UI
            val filterParams = HomeVehicleFilterParams(
                vehicles = next.vehicles,
                userLocation = next.userLocation,
                activeReservation = next.activeReservation,
                activeRental = next.activeRental,
                pendingRental = next.pendingRental,
                selectedVehicleType = next.selectedVehicleType,
                maxDailyPrice = next.maxDailyPrice,
                minRangeKm = next.minRangeKm,
                selectedVehicleId = next.selectedVehicleId
            )
            
            val computedData = filterHomeVehiclesUseCase.invoke(filterParams)

            // If selected ID was cleared because it is no longer visible in filtered vehicles
            // We should adjust it if the updateState block didn't
            val selectedStillVisible = computedData.filteredVehicles.any { it.id == next.selectedVehicleId }
            val finalSelectedId = if (selectedStillVisible || next.selectedVehicleId == null) {
                next.selectedVehicleId
            } else {
                null
            }

            val finalComputedData = if (!selectedStillVisible && next.selectedVehicleId != null) {
                // re-evaluate highlighted vehicle if selected vehicle is cleared
                filterHomeVehiclesUseCase.invoke(filterParams.copy(selectedVehicleId = null))
            } else {
                computedData
            }

            next.copy(
                selectedVehicleId = finalSelectedId,
                filteredVehicles = finalComputedData.filteredVehicles,
                visibleVehicles = finalComputedData.visibleVehicles,
                nearbyVehicles = finalComputedData.nearbyVehicles,
                actionableNearbyVehicles = finalComputedData.actionableNearbyVehicles,
                selectedVehicle = finalComputedData.selectedVehicle,
                activeReservationVehicle = finalComputedData.activeReservationVehicle,
                activeRentalVehicle = finalComputedData.activeRentalVehicle,
                pendingRentalVehicle = finalComputedData.pendingRentalVehicle,
                highlightedVehicle = finalComputedData.highlightedVehicle,
                distanceInfoMap = finalComputedData.distanceInfoMap
            )
        }
    }

    private fun List<com.example.rencar_pair.domain.model.Vehicle>.withReservationState(
        reservation: com.example.rencar_pair.domain.model.Reservation?
    ): List<com.example.rencar_pair.domain.model.Vehicle> {
        return map { vehicle ->
            when {
                reservation?.vehicleId == vehicle.id -> vehicle.copy(
                    status = VehicleStatus.Reserved,
                    canReserve = false,
                    canUnlock = true
                )
                reservation != null -> vehicle.copy(canReserve = false, canUnlock = false)
                else -> vehicle
            }
        }
    }

    private fun List<Rental>.latestPreparingRental(): Rental? {
        return filter { it.status == RentalStatus.Preparing }
            .maxByOrNull { it.createdAt }
    }
}
