package com.example.rencar_pair.data.repository

import com.example.rencar_pair.BuildConfig
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
    private val endpointFallback = FakeWalletRepository()

    override suspend fun getWalletInfo(): NetworkResult<WalletInfo> {
        val result = safeApiCall(
            call = { api.getWalletInfo() },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.getWalletInfo()
        }
    }

    override suspend fun getBalance(): NetworkResult<Double> {
        val result = safeApiCall(
            call = { api.getWalletInfo() },
            transform = { it.currentBalance ?: it.balance ?: 0.0 }
        )
        return result.withEndpointFallback {
            endpointFallback.getBalance()
        }
    }

    override suspend fun topUp(amount: Double): NetworkResult<WalletInfo> {
        if (amount <= 0) {
            return NetworkResult.Error("Gecersiz tutar")
        }
        val result = safeApiCall(
            call = {
                api.topUpWallet(
                    TopUpWalletRequest(
                        amount = amount
                    )
                )
            },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.topUp(amount)
        }
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

    private suspend fun <T> NetworkResult<T>.withEndpointFallback(
        fallback: suspend () -> NetworkResult<T>
    ): NetworkResult<T> {
        return if (BuildConfig.DEBUG && this is NetworkResult.Error && code in ENDPOINT_NOT_READY_CODES) {
            fallback()
        } else {
            this
        }
    }

    private companion object {
        val ENDPOINT_NOT_READY_CODES = setOf(404, 405, 501)
    }
}
