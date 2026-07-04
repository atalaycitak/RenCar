package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String
)
