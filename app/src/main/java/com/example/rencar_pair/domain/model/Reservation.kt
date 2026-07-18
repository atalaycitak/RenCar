package com.example.rencar_pair.domain.model

import java.time.Instant

enum class ReservationStatus {
    Active, Converted, Cancelled, Expired, Unknown;

    companion object {
        fun fromApiString(value: String?): ReservationStatus = when (value?.uppercase()) {
            "ACTIVE" -> Active
            "CONVERTED" -> Converted
            "CANCELLED" -> Cancelled
            "EXPIRED" -> Expired
            else -> Unknown
        }
    }
}

data class Reservation(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val status: ReservationStatus,
    val expiresAt: Instant,
    val remainingSeconds: Int,
    val createdAt: Instant,
    val vehicle: Vehicle? = null
)
