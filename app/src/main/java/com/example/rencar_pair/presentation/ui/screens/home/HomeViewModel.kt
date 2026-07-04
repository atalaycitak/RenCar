package com.example.rencar_pair.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.location.LocationTracker
import com.example.rencar_pair.domain.usecase.GetAvailableVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAvailableVehiclesUseCase: GetAvailableVehiclesUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        onIntent(HomeIntent.LoadVehicles)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadVehicles -> loadVehicles()
            is HomeIntent.SelectVehicle -> _state.update { it.copy(selectedVehicleId = intent.id) }
            is HomeIntent.LocationPermissionChanged -> {
                _state.update {
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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getAvailableVehiclesUseCase()) {
                is NetworkResult.Success -> _state.update {
                    it.copy(
                        vehicles = result.data,
                        selectedVehicleId = result.data.firstOrNull()?.id,
                        isLoading = false
                    )
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun fetchUserLocation() {
        viewModelScope.launch {
            val location = locationTracker.getCurrentLocation()
            _state.update { it.copy(userLocation = location) }
        }
    }
}
