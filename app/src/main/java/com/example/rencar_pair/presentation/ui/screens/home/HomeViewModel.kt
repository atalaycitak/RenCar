package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
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

    private fun loadVehicles() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getAvailableVehicles()) {
                is NetworkResult.Success -> updateState {
                    it.copy(
                        vehicles = result.data,
                        selectedVehicleId = result.data.firstOrNull()?.id,
                        isLoading = false
                    )
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
}
