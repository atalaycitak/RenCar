package com.example.rencar_pair.domain.model

import java.time.Instant

/**
 * POST /rentals/{id}/finish yanıtının domain modeli.
 * Ödeme ekranına fiyat kırılımı ve toplam tutar bilgisi taşır.
 */
data class FinishedRental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: RentalPlan,
    /** Toplam ücret — finish sonrası hesaplanır. */
    val totalPrice: Double?,
    /** Açılış ücreti. */
    val startFee: Double?,
    /** Servis ücreti — kullanımın yüzdesi. */
    val serviceFee: Double?,
    /** Kullanım ücreti kalemi (PER_MINUTE/HOURLY/DAILY formülüne göre). */
    val usageFee: Double?,
    /** Ödeme indirim tutarı. */
    val discountAmount: Double?,
    val distanceKm: Double?,
    val durationMinutes: Double?,
    /** Yolculuğun toplam süresi (saniye). */
    val elapsedSeconds: Double?,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    val startedAt: Instant?,
    val endedAt: Instant?,
    val createdAt: Instant
)
