package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.VehicleRepository

class GetAvailableVehiclesUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke() = repository.getAvailableVehicles()
}
