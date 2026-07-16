package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponse(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val rangeKm: Int? = null,
    val locationName: String? = null,
    val fuelLevelPercent: Int? = null,
    val fuelPercent: Int? = null,
    val transmission: String? = null,
    val seatCount: Int? = null,
    val seats: Int? = null,
    val imageUrl: String? = null,
    val pricePerMinute: Double? = null,
    val pricePerHour: Double? = null,
    val segment: String? = null,
    val canReserve: Boolean? = null,
    val canUnlock: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
