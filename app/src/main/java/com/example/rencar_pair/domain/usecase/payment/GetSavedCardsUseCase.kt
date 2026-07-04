package com.example.rencar_pair.domain.usecase.payment

import com.example.rencar_pair.domain.repository.PaymentRepository

class GetSavedCardsUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke() = repository.getSavedCards()
}
