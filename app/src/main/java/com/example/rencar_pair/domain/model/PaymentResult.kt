package com.example.rencar_pair.domain.model

data class PaymentResult(
    val transactionId: String? = null,
    val errorMessage: String? = null,
    val rentalId: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.Unknown,
    val method: PaymentMethod? = null,
    val totalPrice: Double? = null,
    val discountAmount: Double = 0.0,
    val paidAmount: Double? = null,
    val walletBalance: Double? = null,
    val card: PaidCardSummary? = null
)

data class PaidCardSummary(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int? = null,
    val expYear: Int? = null
)
