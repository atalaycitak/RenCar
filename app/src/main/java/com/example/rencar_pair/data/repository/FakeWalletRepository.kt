package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.WalletInfo
import com.example.rencar_pair.domain.model.WalletTransaction
import com.example.rencar_pair.domain.model.WalletTransactionType
import com.example.rencar_pair.domain.repository.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeWalletRepository : WalletRepository {
    private val mutex = Mutex()
    private var currentBalance = 1500.0
    private val transactions = mutableListOf(
        WalletTransaction(
            id = "wtx_001",
            amount = 500.0,
            createdAt = "2026-07-17T10:00:00.000Z",
            type = WalletTransactionType.TOP_UP
        )
    )

    override suspend fun getWalletInfo(): NetworkResult<WalletInfo> {
        delay(800)
        return mutex.withLock {
            NetworkResult.Success(
                WalletInfo(
                    balance = currentBalance,
                    transactions = transactions.toList().sortedByDescending { it.createdAt }
                )
            )
        }
    }

    override suspend fun getBalance(): NetworkResult<Double> {
        delay(500)
        return mutex.withLock { NetworkResult.Success(currentBalance) }
    }

    override suspend fun topUp(amount: Double): NetworkResult<WalletInfo> {
        delay(1200)
        if (amount <= 0) {
            return NetworkResult.Error("Geçersiz tutar")
        }
        return mutex.withLock {
            currentBalance += amount
            transactions.add(
                WalletTransaction(
                    id = "wtx_${System.currentTimeMillis()}",
                    amount = amount,
                    createdAt = java.time.Instant.now().toString(),
                    type = WalletTransactionType.TOP_UP
                )
            )
            NetworkResult.Success(
                WalletInfo(
                    balance = currentBalance,
                    transactions = transactions.toList().sortedByDescending { it.createdAt }
                )
            )
        }
    }
}
