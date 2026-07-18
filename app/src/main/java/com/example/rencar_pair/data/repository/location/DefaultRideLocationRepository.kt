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
            val vehicle = listOfNotNull(
                envelope.optJSONObject("vehicle"),
                envelope.optJSONObject("data")?.optJSONObject("vehicle"),
                envelope.optJSONObject("data")?.optJSONObject("location"),
                envelope.optJSONObject("data"),
                envelope.optJSONObject("location"),
                envelope
            ).firstOrNull { it.has("latitude") || it.has("lat") } ?: envelope
            
            val latitude = vehicle.optFiniteDouble("latitude")
                ?: vehicle.optFiniteDouble("lat")
                ?: return@mapNotNull null
            val longitude = vehicle.optFiniteDouble("longitude")
                ?: vehicle.optFiniteDouble("lng")
                ?: vehicle.optFiniteDouble("lon")
                ?: return@mapNotNull null
            
            VehiclePoint(latitude, longitude)
        }
    }

    private fun JSONObject.optFiniteDouble(name: String): Double? {
        if (!has(name)) return null
        return optDouble(name, Double.NaN).takeIf { it.isFinite() }
    }
}
