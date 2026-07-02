package com.example.rencar_pair.presentation.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.usecase.GetVehicleDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleDetailViewModel(
    private val vehicleId: String,
    private val getVehicleDetailUseCase: GetVehicleDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        onIntent(VehicleDetailIntent.LoadVehicle)
    }

    fun onIntent(intent: VehicleDetailIntent) {
        when (intent) {
            VehicleDetailIntent.LoadVehicle -> loadVehicle()
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getVehicleDetailUseCase(vehicleId)) {
                is NetworkResult.Success -> _state.update {
                    it.copy(vehicle = result.data, isLoading = false)
                }
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }
}
