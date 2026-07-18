package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationRequest(
    val vehicleId: String
)

/**
 * Rezervasyon yanıtında gelen araç özeti.
 * Konum ve dakika bazlı fiyat bilgisi içerir.
 */
@Serializable
data class ReservationVehicleSummaryResponse(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pricePerMinute: Double? = null
)

@Serializable
data class ReservationResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    /** Araç özeti — v2 API'de dolu. */
    val vehicle: ReservationVehicleSummaryResponse? = null,
    /** ACTIVE | CONVERTED | CANCELLED | EXPIRED */
    val status: String,
    val expiresAt: String,
    val remainingSeconds: Int,
    val createdAt: String
)
