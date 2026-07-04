package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: Instant,
    val endDate: Instant,
    val totalPrice: Double,
    val status: String
)
