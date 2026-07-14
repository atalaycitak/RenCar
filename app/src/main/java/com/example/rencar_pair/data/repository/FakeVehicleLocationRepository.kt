package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.repository.VehicleLocationStreamMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeVehicleLocationRepository : VehicleLocationRepository {
    override val streamMode: VehicleLocationStreamMode = VehicleLocationStreamMode.Demo

    override fun observeVehiclePositions(): Flow<List<VehiclePosition>> = flow {
        var tick = 0
        while (true) {
            val offset = (tick % 6) * 0.0004
            emit(
                listOf(
                    VehiclePosition("v1", 41.0082 + offset, 28.9784 + offset, VehicleStatus.Available),
                    VehiclePosition("v2", 41.0390 - offset, 28.9835 + offset, VehicleStatus.Available),
                    VehiclePosition("v3", 41.0565, 29.0320 - offset, VehicleStatus.Available)
                )
            )
            tick += 1
            delay(5000)
        }
    }
}
