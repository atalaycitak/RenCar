package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateReservationRequest(
    val vehicleId: String
)

@Serializable
data class ReservationResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val status: String,
    val expiresAt: String,
    val remainingSeconds: Int,
    val createdAt: String
)
