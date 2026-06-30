package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getAvailableVehicles(): NetworkResult<List<Vehicle>>
}
