package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.model.VehiclePoint
import com.example.rencar_pair.domain.repository.RideLocationRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveVehicleLocationUseCase(
    private val repository: RideLocationRepository
) {
    operator fun invoke(): Flow<VehiclePoint> {
        return repository.observeActiveVehicleLocation()
    }
}
