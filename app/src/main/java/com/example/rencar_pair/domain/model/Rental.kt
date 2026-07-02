package com.example.rencar_pair.domain.model

data class Rental(
    val id: String,
    val vehicleId: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String
)
