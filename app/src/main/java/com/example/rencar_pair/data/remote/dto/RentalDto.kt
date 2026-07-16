package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRentalRequest(
    val vehicleId: String,
    val plan: String? = null,
    val endDate: String? = null
)

@Serializable
data class RentalResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: String? = null,
    val startedAt: String? = null,
    val endDate: String? = null,
    val endedAt: String? = null,
    val totalPrice: Double? = null,
    val status: String,
    val createdAt: String
)
