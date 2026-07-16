package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getAvailableVehicles(
        type: String? = null,
        page: Int? = null,
        limit: Int? = null,
        includeBusy: Boolean = false
    ): NetworkResult<List<Vehicle>>
    suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle>
}
