package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.VehicleRepository

class VehicleUseCases(
    private val repository: VehicleRepository
) {
    suspend fun getAvailableVehicles(
        type: String? = null,
        page: Int? = null,
        limit: Int? = null
    ) = repository.getAvailableVehicles(type, page, limit)

    suspend fun getVehicleDetail(id: String) = repository.getVehicleDetail(id)
}
