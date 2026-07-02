package com.example.rencar_pair.domain.usecase.payment

import com.example.rencar_pair.domain.repository.WalletRepository

class GetWalletBalanceUseCase(private val repository: WalletRepository) {
    suspend operator fun invoke() = repository.getBalance()
}
