package com.example.rencar_pair.domain.model

import java.time.Instant

/**
 * GET /rentals/active yanıtının domain modeli.
 * Aktif kiralama ekranına anlık süre ve maliyet bilgisi taşır.
 */
data class ActiveRental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: RentalPlan,
    val status: RentalStatus,
    /** Yolculuk başlangıcından bu yana geçen süre (saniye). */
    val elapsedSeconds: Double,
    /** Yolculuk şu an bitirilse ödenecek tahmini tutar. */
    val currentCost: Double,
    val startedAt: Instant?,
    val distanceKm: Double?,
    val durationMinutes: Double?,
    val startFee: Double?,
    val createdAt: Instant
)
