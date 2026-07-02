package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.WalletInfo

interface WalletRepository {
    suspend fun getWalletInfo(): NetworkResult<WalletInfo>
    suspend fun getBalance(): NetworkResult<Double>
    suspend fun topUp(amount: Double, cardToken: String): NetworkResult<WalletInfo>
}
