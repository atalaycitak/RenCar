package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class ReservationQuote(
    val vehicleId: String,
    val days: Int,
    val pricePerDay: Double,
    val serviceFee: Double,
    val deliveryFee: Double,
    val totalPrice: Double,
    val endDateIso: String
) {
    val subtotal: Double = pricePerDay * days
}
