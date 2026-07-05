package com.example.rencar_pair.domain.model

import java.time.Instant

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: Instant,
    val endDate: Instant,
    val totalPrice: Double,
    val status: String
)
