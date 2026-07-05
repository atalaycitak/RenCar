package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakePaymentRepository : PaymentRepository {
    private val mutex = Mutex()
    private val fakeCards = mutableListOf(
        PaymentMethod(
            cardToken = "tok_12345",
            cardAlias = "Garanti Bonus",
            binNumber = "493827",
            cardAssociation = "VISA"
        ),
        PaymentMethod(
            cardToken = "tok_67890",
            cardAlias = "IsBankasi Maximum",
            binNumber = "543210",
            cardAssociation = "MASTER_CARD"
        )
    )

    override suspend fun processPayment(
        rentalId: String,
        cardToken: String,
        amount: Double
    ): NetworkResult<PaymentResult> {
        delay(1000)
        if (amount <= 0) {
            return NetworkResult.Error("Geçersiz tutar")
        }
        return NetworkResult.Success(
            PaymentResult(
                status = "SUCCESS",
                transactionId = "tx_${System.currentTimeMillis()}",
                errorMessage = null
            )
        )
    }

    override suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<PaymentMethod> {
        delay(1500)
        if (cardNumber.length < 16) {
            return NetworkResult.Error("Geçersiz kart numarası")
        }
        val newCard = PaymentMethod(
            cardToken = "tok_${System.currentTimeMillis()}",
            cardAlias = cardHolderName,
            binNumber = cardNumber.take(6),
            cardAssociation = if (cardNumber.startsWith("4")) "VISA" else "MASTER_CARD"
        )
        mutex.withLock { fakeCards.add(newCard) }
        return NetworkResult.Success(newCard)
    }

    override suspend fun getSavedCards(): NetworkResult<List<PaymentMethod>> {
        delay(800)
        return mutex.withLock { NetworkResult.Success(fakeCards.toList()) }
    }
}
