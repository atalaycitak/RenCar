package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.model.VehiclePosition
import kotlinx.coroutines.flow.Flow

interface VehicleLocationRepository {
    fun observeVehiclePositions(): Flow<List<VehiclePosition>>
}
