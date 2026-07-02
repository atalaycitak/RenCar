package com.example.rencar_pair.domain.usecase.payment

import com.example.rencar_pair.domain.repository.WalletRepository

class TopUpWalletUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke(amount: Double, cardToken: String) =
        repository.topUp(amount, cardToken)
}
