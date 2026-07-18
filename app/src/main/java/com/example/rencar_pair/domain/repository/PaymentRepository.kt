package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.SavedCard

interface PaymentRepository {
    suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null,
        discountCode: String? = null,
        iyzicoPaymentId: String? = null
    ): NetworkResult<PaymentResult>

    suspend fun processPayment(
        rentalId: String,
        cardToken: String,
        amount: Double
    ): NetworkResult<PaymentResult> {
        if (amount <= 0) {
            return NetworkResult.Error("Geçersiz tutar")
        }
        return payRental(
            rentalId = rentalId,
            method = PaymentMethod.Card,
            cardId = cardToken
        )
    }

    suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<SavedCard>

    suspend fun getSavedCards(): NetworkResult<List<SavedCard>>
    suspend fun setDefaultCard(cardId: String): NetworkResult<SavedCard>
    suspend fun deleteCard(cardId: String): NetworkResult<Unit>
}
