package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class PaymentMethod(
    val cardToken: String,
    val cardAlias: String,
    val binNumber: String,
    val cardAssociation: String
)
