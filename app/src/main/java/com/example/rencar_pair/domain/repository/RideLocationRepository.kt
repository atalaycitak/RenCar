package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.model.VehiclePoint
import kotlinx.coroutines.flow.Flow

interface RideLocationRepository {
    fun observeActiveVehicleLocation(): Flow<VehiclePoint>
}
