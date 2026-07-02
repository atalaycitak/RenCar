package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RejectLicenseRequest(
    val reason: String
)

@Serializable
data class AdminLicenseResponse(
    val id: String,
    val userId: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val user: AuthUserResponse
)

@Serializable
data class CreateVehicleRequest(
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val latitude: Double,
    val longitude: Double,
    val status: String? = null
)

@Serializable
data class UpdateVehicleRequest(
    val plate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val type: String? = null,
    val pricePerDay: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null
)

@Serializable
data class RentalUserSummaryResponse(
    val id: String,
    val email: String,
    val fullName: String
)

@Serializable
data class RentalVehicleSummaryResponse(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val status: String
)

@Serializable
data class AdminRentalResponse(
    val id: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val user: RentalUserSummaryResponse,
    val vehicle: RentalVehicleSummaryResponse
)

@Serializable
data class VehiclePositionResponse(
    val vehicleId: String,
    val plate: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: String
)

