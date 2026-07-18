package com.example.rencar_pair.domain.model

/**
 * Tek seferlik ödeme işleminin sonucu.
 * PaymentStatus (Unpaid/Paid) için Rental.kt dosyasını kullanın.
 */
data class PaymentResult(
    val transactionId: String?,
    val errorMessage: String?
)
