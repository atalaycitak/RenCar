package com.example.rencar_pair.domain.model

import java.time.Instant

enum class RentalStatus {
    Active, Completed, Cancelled, Unknown;

    companion object {
        fun fromApiString(value: String?): RentalStatus = when (value?.uppercase()) {
            "ACTIVE" -> Active
            "COMPLETED" -> Completed
            "CANCELLED" -> Cancelled
            else -> Unknown
        }
    }
}

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: Instant,
    val endDate: Instant,
    val totalPrice: Double,
    val status: RentalStatus
)
