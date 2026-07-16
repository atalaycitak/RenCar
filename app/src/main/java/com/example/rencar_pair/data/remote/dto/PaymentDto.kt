package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProcessPaymentRequest(
    val method: String,
    val cardId: String? = null,
    val discountCode: String? = null,
    val iyzicoPaymentId: String? = null
)

@Serializable
data class ProcessPaymentResponse(
    val rentalId: String? = null,
    val paymentStatus: String? = null,
    val method: String? = null,
    val totalPrice: Double? = null,
    val discountAmount: Double? = null,
    val paidAmount: Double? = null,
    val walletBalance: Double? = null,
    val status: String? = null,
    val transactionId: String? = null,
    val errorMessage: String? = null
)

@Serializable
data class AddCardRequest(
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int
)

@Serializable
data class IyzicoCardTokenResponse(
    val id: String? = null,
    val brand: String? = null,
    val last4: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val isDefault: Boolean? = null,
    val createdAt: String? = null,
    val cardToken: String? = null,
    val cardAlias: String? = null,
    val binNumber: String? = null,
    val cardAssociation: String? = null
)
