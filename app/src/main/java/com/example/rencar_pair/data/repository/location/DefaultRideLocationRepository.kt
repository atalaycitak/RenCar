package com.example.rencar_pair.data.repository.location

import com.example.rencar_pair.data.socket.RenCarSocketClient
import com.example.rencar_pair.domain.model.VehiclePoint
import com.example.rencar_pair.domain.repository.RideLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.json.JSONObject

class DefaultRideLocationRepository(
    private val socketClient: RenCarSocketClient
) : RideLocationRepository {

    override fun observeActiveVehicleLocation(): Flow<VehiclePoint> {
        return socketClient.observeEvent(
            namespace = "https://rencarv2.halitkalayci.com/ws/locations",
            eventName = "my-vehicle"
        ).mapNotNull { envelope ->
            val vehicle = envelope.optJSONObject("vehicle") ?: envelope
            
            val latitude = vehicle.optDouble("latitude", Double.NaN)
            val longitude = vehicle.optDouble("longitude", Double.NaN)
            
            if (!latitude.isFinite() || !longitude.isFinite()) {
                null
            } else {
                VehiclePoint(latitude, longitude)
            }
        }
    }
}
