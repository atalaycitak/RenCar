package com.example.rencar_pair.domain.model

/**
 * Kullanıcının kayıtlı kartlarını temsil eder.
 * Eski adı PaymentMethod idi; PaymentMethod artık enum olarak Rental.kt içinde tanımlıdır.
 */
data class SavedCard(
    val cardToken: String,
    val cardAlias: String,
    val binNumber: String,
    val cardAssociation: String
)
