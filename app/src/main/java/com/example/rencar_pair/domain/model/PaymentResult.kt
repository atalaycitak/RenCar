package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class PaymentResult(
    val status: String,
    val transactionId: String?,
    val errorMessage: String?
)
