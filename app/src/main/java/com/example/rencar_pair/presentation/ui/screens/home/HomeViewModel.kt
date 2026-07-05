package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel
import com.example.rencar_pair.presentation.mvi.NoEffect

class HomeViewModel(
    private val vehicleUseCases: VehicleUseCases,
    private val locationTracker: LocationTracker
) : BaseMviViewModel<HomeState, HomeIntent, NoEffect>(HomeState()) {

    init {
        onIntent(HomeIntent.LoadVehicles)
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
                    it.copy(locationPermissionGranted = intent.granted)
                }
                if (intent.granted) {
                    fetchUserLocation()
                }
            }
            HomeIntent.FetchUserLocation -> fetchUserLocation()
        }
    }

    private fun loadVehicles(type: VehicleType? = currentState().selectedVehicleType) {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getAvailableVehicles(type = type?.toApiQuery())) {
                is NetworkResult.Success -> updateState {
                    val next = it.copy(
                        vehicles = result.data,
                        isLoading = false
                    )
                    next.copy(selectedVehicleId = next.firstVisibleVehicleId())
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
