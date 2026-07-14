package com.example.rencar_pair.data.repository

import com.example.rencar_pair.BuildConfig
import com.example.rencar_pair.data.remote.dto.VehiclePositionResponse
import com.example.rencar_pair.domain.model.VehiclePosition
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.repository.VehicleLocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DefaultVehicleLocationRepository(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : VehicleLocationRepository {

    override fun observeVehiclePositions(): Flow<List<VehiclePosition>> {
        val url = BuildConfig.VEHICLE_LOCATION_WS_URL.takeIf { it.isNotBlank() } ?: return emptyFlow()

        return callbackFlow {
            val request = Request.Builder().url(url).build()
            val listener = object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    parsePositions(text)?.let { trySend(it) }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    close(t)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    close()
                }
            }
            val socket = okHttpClient.newWebSocket(request, listener)
            awaitClose { socket.close(1000, "Map stream stopped") }
        }
    }

    private fun parsePositions(raw: String): List<VehiclePosition>? {
        return runCatching {
            json.decodeFromString(ListSerializer(VehiclePositionResponse.serializer()), raw)
                .map { it.toDomain() }
        }.getOrElse {
            runCatching {
                listOf(json.decodeFromString(VehiclePositionResponse.serializer(), raw).toDomain())
            }.getOrElse {
                runCatching {
                    json.decodeFromString(VehiclePositionEnvelope.serializer(), raw)
                        .positions
                        .map { it.toDomain() }
                }.getOrNull()
            }
        }
    }

    private fun VehiclePositionResponse.toDomain(): VehiclePosition {
        return VehiclePosition(
            vehicleId = vehicleId,
            latitude = latitude,
            longitude = longitude,
            status = VehicleStatus.fromApiString(status),
            updatedAt = updatedAt
        )
    }
}

@Serializable
private data class VehiclePositionEnvelope(
    val positions: List<VehiclePositionResponse> = emptyList()
)
