package com.example.rencar_pair.domain.usecase.payment

import com.example.rencar_pair.domain.repository.PaymentRepository

class ProcessPaymentUseCase(private val repository: PaymentRepository) {
    suspend operator fun invoke(rentalId: String, cardToken: String, amount: Double) =
        repository.processPayment(rentalId, cardToken, amount)
}
