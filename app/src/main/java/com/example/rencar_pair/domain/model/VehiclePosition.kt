package com.example.rencar_pair.domain.model

data class VehiclePosition(
    val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val status: VehicleStatus,
    val updatedAt: String? = null
)
