package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.model.VehiclePosition
import kotlinx.coroutines.flow.Flow

interface VehicleLocationRepository {
    val streamMode: VehicleLocationStreamMode
        get() = VehicleLocationStreamMode.Inactive

    fun setActiveVehicleId(vehicleId: String?) = Unit

    fun observeVehiclePositions(): Flow<List<VehiclePosition>>
}

enum class VehicleLocationStreamMode {
    Inactive,
    WebSocket,
    Demo
}
