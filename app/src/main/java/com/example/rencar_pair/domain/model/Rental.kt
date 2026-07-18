package com.example.rencar_pair.domain.model

import java.time.Instant

enum class RentalStatus {
    Preparing, Active, Completed, Cancelled, Unknown;

    companion object {
        fun fromApiString(value: String?): RentalStatus = when (value?.uppercase()) {
            "PREPARING" -> Preparing
            "ACTIVE" -> Active
            "COMPLETED" -> Completed
            "CANCELLED" -> Cancelled
            else -> Unknown
        }
    }
}

enum class RentalPlan {
    PerMinute, Hourly, Daily, Unknown;

    companion object {
        fun fromApiString(value: String?): RentalPlan = when (value?.uppercase()) {
            "PER_MINUTE" -> PerMinute
            "HOURLY" -> Hourly
            "DAILY" -> Daily
            else -> Unknown
        }
    }

    fun toApiValue(): String = when (this) {
        PerMinute -> "PER_MINUTE"
        Hourly -> "HOURLY"
        Daily -> "DAILY"
        Unknown -> "DAILY"
    }
}

enum class PaymentStatus {
    Unpaid, Paid, Unknown;

    companion object {
        fun fromApiString(value: String?): PaymentStatus = when (value?.uppercase()) {
            "UNPAID" -> Unpaid
            "PAID" -> Paid
            else -> Unknown
        }
    }
}

enum class PaymentMethod {
    Wallet, Card, Iyzico;

    fun toApiValue(): String = when (this) {
        Wallet -> "WALLET"
        Card -> "CARD"
        Iyzico -> "IYZICO"
    }

    companion object {
        fun fromApiString(value: String?): PaymentMethod? = when (value?.uppercase()) {
            "WALLET" -> Wallet
            "CARD" -> Card
            "IYZICO" -> Iyzico
            else -> null
        }
    }
}

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: RentalPlan,
    val status: RentalStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    /** Toplam ücret — PER_MINUTE/HOURLY'de finish'e kadar null, DAILY'de baştan kilitlenir. */
    val totalPrice: Double?,
    val startFee: Double?,
    val serviceFee: Double?,
    val distanceKm: Double?,
    val durationMinutes: Double?,
    val discountAmount: Double?,
    val startedAt: Instant?,
    val endedAt: Instant?,
    /** DAILY plana özel planlanan iade tarihi. */
    val scheduledEndDate: Instant?,
    val createdAt: Instant
)
