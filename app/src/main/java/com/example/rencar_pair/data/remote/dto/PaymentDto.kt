package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProcessPaymentRequest(
    val rentalId: String,
    val cardToken: String,
    val amount: Double
)

@Serializable
data class ProcessPaymentResponse(
    val status: String,
    val transactionId: String?,
    val errorMessage: String?
)

@Serializable
data class AddCardRequest(
    val cardNumber: String,
    val expireMonth: String,
    val expireYear: String,
    val cvc: String,
    val cardHolderName: String
)

@Serializable
data class IyzicoCardTokenResponse(
    val cardToken: String,
    val cardAlias: String,
    val binNumber: String,
    val cardAssociation: String
)
