package com.example.rencar_pair.domain.model

data class RentalSession(
    val id: String,
    val vehicleId: String,
    val startTime: Long,
    val elapsedMinutes: Int,
    val currentCost: Double,
    val distanceKm: Double,
    val isActive: Boolean
)
