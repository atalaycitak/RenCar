package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.TokenHolder
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import com.example.rencar_pair.domain.repository.VehicleLocationStreamMode
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.json.JSONObject
import java.net.URI

class DefaultVehicleLocationRepository(
    private val tokenHolder: TokenHolder
) : VehicleLocationRepository {

    @Volatile
    private var activeVehicleId: String? = null

    override val streamMode: VehicleLocationStreamMode
        get() = if (tokenHolder.token.isNullOrBlank()) {
            VehicleLocationStreamMode.Inactive
        } else {
            VehicleLocationStreamMode.WebSocket
        }

    override fun setActiveVehicleId(vehicleId: String?) {
        activeVehicleId = vehicleId
    }

    override fun observeVehiclePositions(): Flow<List<VehiclePosition>> {
        val token = tokenHolder.token ?: return emptyFlow()

        return callbackFlow {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setTransports(arrayOf(WebSocket.NAME))
                .setForceNew(true)
                .build()
            val socket = IO.socket(URI.create(SOCKET_NAMESPACE), options)
            val locationListener = Emitter.Listener { args ->
                parseMyVehicleEvent(args.firstOrNull())?.let { position ->
                    trySend(listOf(position))
                }
            }
            val connectErrorListener = Emitter.Listener { args ->
                close(IllegalStateException(args.firstOrNull()?.toString() ?: "Socket.IO connect error"))
            }

            socket.on(MY_VEHICLE_EVENT, locationListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorListener)
            socket.connect()

            awaitClose {
                socket.off(MY_VEHICLE_EVENT, locationListener)
                socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorListener)
                socket.disconnect()
            }
        }
    }

    private fun parseMyVehicleEvent(raw: Any?): VehiclePosition? {
        val envelope = when (raw) {
            is JSONObject -> raw
            is String -> runCatching { JSONObject(raw) }.getOrNull()
            else -> null
        } ?: return null

        val vehicle = listOfNotNull(
            envelope.optJSONObject("vehicle"),
            envelope.optJSONObject("data")?.optJSONObject("vehicle"),
            envelope.optJSONObject("data")?.optJSONObject("location"),
            envelope.optJSONObject("data"),
            envelope.optJSONObject("location"),
            envelope
        ).firstOrNull { it.has("latitude") || it.has("lat") } ?: envelope
        val vehicleId = vehicle.optString("vehicleId", vehicle.optString("id"))
            .ifBlank { activeVehicleId.orEmpty() }
        if (vehicleId.isBlank()) return null

        val latitude = vehicle.optFiniteDouble("latitude") ?: vehicle.optFiniteDouble("lat") ?: return null
        val longitude = vehicle.optFiniteDouble("longitude")
            ?: vehicle.optFiniteDouble("lng")
            ?: vehicle.optFiniteDouble("lon")
            ?: return null

        return VehiclePosition(
            vehicleId = vehicleId,
            latitude = latitude,
            longitude = longitude,
            status = VehicleStatus.fromApiString(vehicle.optString("status", "RENTED")),
            updatedAt = envelope.optString("ts", vehicle.optString("updatedAt")).takeIf { it.isNotBlank() }
        )
    }

    private fun JSONObject.optFiniteDouble(name: String): Double? {
        if (!has(name)) return null
        return optDouble(name, Double.NaN).takeIf { it.isFinite() }
    }

    private companion object {
        const val SOCKET_NAMESPACE = "https://rencarv2.halitkalayci.com/ws/locations"
        const val MY_VEHICLE_EVENT = "my-vehicle"
    }
}
