package com.example.rencar_pair.domain.model

data class WalletInfo(
    val balance: Double,
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    val id: String,
    val amount: Double,
    /** ISO tarih string (ör. "2026-07-11T12:00:00.000Z"). */
    val createdAt: String,
    val type: WalletTransactionType,
    val rentalId: String? = null,
    val description: String? = null
)

enum class WalletTransactionType {
    TOP_UP,
    RENTAL_PAYMENT,
    REFERRAL_BONUS
}
