package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopUpWalletRequest(
    val amount: Double,
    val cardToken: String
)

@Serializable
data class WalletBalanceResponse(
    val currentBalance: Double? = null,
    val balance: Double? = null
)

@Serializable
data class WalletTransactionDto(
    val id: String,
    val amount: Double,
    val date: Long,
    val type: String
)

@Serializable
data class WalletInfoResponse(
    val balance: Double? = null,
    val currentBalance: Double? = null,
    val transactions: List<WalletTransactionDto> = emptyList()
)
