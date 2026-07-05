package com.example.rencar_pair.presentation.ui.screens.vehicle

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.VehicleUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class VehicleDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val vehicleUseCases: VehicleUseCases
) : BaseMviViewModel<VehicleDetailState, VehicleDetailIntent, VehicleDetailEffect>(
    VehicleDetailState(isLoading = true)
) {

    private val vehicleId: String = savedStateHandle.get<String>("vehicleId")!!

    init {
        onIntent(VehicleDetailIntent.LoadVehicle)
    }

    override fun onIntent(intent: VehicleDetailIntent) {
        when (intent) {
            VehicleDetailIntent.LoadVehicle -> loadVehicle()
        }
    }

    private fun loadVehicle() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vehicleUseCases.getVehicleDetail(vehicleId)) {
                is NetworkResult.Success -> updateState {
                    it.copy(vehicle = result.data, isLoading = false)
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
