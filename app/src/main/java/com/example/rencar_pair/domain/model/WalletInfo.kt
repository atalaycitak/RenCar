package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WalletInfo(
    val balance: Double,
    val transactions: List<WalletTransaction>
)

@Immutable
data class WalletTransaction(
    val id: String,
    val amount: Double,
    val date: Long,
    val type: WalletTransactionType
)

enum class WalletTransactionType {
    TOP_UP,
    RENTAL_PAYMENT
}
