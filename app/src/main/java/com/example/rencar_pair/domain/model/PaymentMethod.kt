package com.example.rencar_pair.domain.model

data class PaymentMethod(
    val cardToken: String,
    val cardAlias: String,
    val binNumber: String,
    val cardAssociation: String
)
