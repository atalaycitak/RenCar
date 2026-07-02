package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.VehicleRepository

class GetVehicleDetailUseCase(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String) = repository.getVehicleDetail(id)
}
