package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.TopUpWalletRequest
import com.example.rencar_pair.data.remote.dto.WalletInfoResponse
import com.example.rencar_pair.data.remote.dto.WalletTransactionDto
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.domain.model.WalletTransaction
import com.example.rencar_pair.domain.model.WalletTransactionType
import com.example.rencar_pair.domain.repository.WalletRepository

class DefaultWalletRepository(
    private val api: RenCarApi
) : WalletRepository {
    override suspend fun getWalletInfo(): NetworkResult<WalletInfo> {
        return safeApiCall(
            call = { api.getWalletInfo() },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getBalance(): NetworkResult<Double> {
        return safeApiCall(
            call = { api.getWalletInfo() },
            transform = { it.currentBalance ?: it.balance ?: 0.0 }
        )
    }

    override suspend fun topUp(amount: Double): NetworkResult<WalletInfo> {
        if (amount <= 0) {
            return NetworkResult.Error("Geçersiz tutar")
        }
        return safeApiCall(
            call = {
                api.topUpWallet(
                    TopUpWalletRequest(
                        amount = amount
                    )
                )
            },
            transform = { it.toDomain() }
        )
    }

    private fun WalletInfoResponse.toDomain(): WalletInfo {
        return WalletInfo(
            balance = currentBalance ?: balance ?: 0.0,
            transactions = transactions.map { it.toDomain() }.sortedByDescending { it.createdAt }
        )
    }

    private fun WalletTransactionDto.toDomain(): WalletTransaction {
        return WalletTransaction(
            id = id,
            amount = amount,
            createdAt = createdAt,
            rentalId = rentalId,
            description = description,
            type = when (type.uppercase()) {
                "TOP_UP", "TOPUP" -> WalletTransactionType.TOP_UP
                "RENTAL_PAYMENT" -> WalletTransactionType.RENTAL_PAYMENT
                "REFERRAL_BONUS" -> WalletTransactionType.REFERRAL_BONUS
                else -> WalletTransactionType.RENTAL_PAYMENT
            }
        )
    }
}
