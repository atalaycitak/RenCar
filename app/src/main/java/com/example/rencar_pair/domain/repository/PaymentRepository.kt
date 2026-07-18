package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.SavedCard

interface PaymentRepository {
    suspend fun processPayment(rentalId: String, cardToken: String, amount: Double): NetworkResult<PaymentResult>
    suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<SavedCard>
    suspend fun getSavedCards(): NetworkResult<List<SavedCard>>
}
