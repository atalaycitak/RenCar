package com.example.rencar_pair.domain.model

data class PaymentResult(
    val status: String,
    val transactionId: String?,
    val errorMessage: String?
)
