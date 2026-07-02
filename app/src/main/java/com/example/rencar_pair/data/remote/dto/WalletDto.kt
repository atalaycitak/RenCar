package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopUpWalletRequest(
    val amount: Double,
    val cardToken: String
)

@Serializable
data class WalletBalanceResponse(
    val currentBalance: Double
)

@Serializable
data class WalletTransactionDto(
    val id: String,
    val amount: Double,
    val date: Long,
    val type: String
)
