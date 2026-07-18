package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaidCardSummary
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.PaymentStatus
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakePaymentRepository : PaymentRepository {
    private val mutex = Mutex()
    private val fakeCards = mutableListOf(
        SavedCard(
            cardToken = "tok_12345",
            cardAlias = "Garanti Bonus",
            binNumber = "4291",
            cardAssociation = "VISA",
            expMonth = 8,
            expYear = 2027,
            isDefault = true
        ),
        SavedCard(
            cardToken = "tok_67890",
            cardAlias = "IsBankasi Maximum",
            binNumber = "7740",
            cardAssociation = "MASTERCARD",
            expMonth = 11,
            expYear = 2026
        )
    )

    override suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String?,
        discountCode: String?,
        iyzicoPaymentId: String?
    ): NetworkResult<PaymentResult> {
        delay(1000)
        val selectedCard = mutex.withLock { fakeCards.firstOrNull { it.cardToken == cardId } }
        if (method == PaymentMethod.Card && selectedCard == null) {
            return NetworkResult.Error("Kart secimi gerekli")
        }
        if (method == PaymentMethod.Iyzico && iyzicoPaymentId.isNullOrBlank()) {
            return NetworkResult.Error("Iyzico odeme bilgisi gerekli")
        }

        return NetworkResult.Success(
            PaymentResult(
                transactionId = "tx_${System.currentTimeMillis()}",
                rentalId = rentalId,
                paymentStatus = PaymentStatus.Paid,
                method = method,
                discountAmount = if (discountCode.isNullOrBlank()) 0.0 else 20.0,
                paidAmount = null,
                card = selectedCard?.let {
                    PaidCardSummary(
                        id = it.cardToken,
                        brand = it.cardAssociation,
                        last4 = it.last4,
                        expMonth = it.expMonth,
                        expYear = it.expYear
                    )
                }
            )
        )
    }

    override suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<SavedCard> {
        delay(1500)
        if (cardNumber.length < 16) {
            return NetworkResult.Error("Gecersiz kart numarasi")
        }
        val expMonth = expireMonth.toIntOrNull()
            ?: return NetworkResult.Error("Gecersiz son kullanma ayi")
        val expYear = expireYear.toIntOrNull()
            ?: return NetworkResult.Error("Gecersiz son kullanma yili")

        return mutex.withLock {
            val newCard = SavedCard(
                cardToken = "tok_${System.currentTimeMillis()}",
                cardAlias = cardHolderName.ifBlank { "Yeni kart" },
                binNumber = cardNumber.takeLast(4),
                cardAssociation = if (cardNumber.startsWith("4")) "VISA" else "MASTERCARD",
                expMonth = expMonth,
                expYear = expYear,
                isDefault = fakeCards.isEmpty()
            )
            fakeCards.add(newCard)
            NetworkResult.Success(newCard)
        }
    }

    override suspend fun getSavedCards(): NetworkResult<List<SavedCard>> {
        delay(800)
        return mutex.withLock { NetworkResult.Success(fakeCards.toList()) }
    }

    override suspend fun setDefaultCard(cardId: String): NetworkResult<SavedCard> {
        delay(400)
        return mutex.withLock {
            val selectedCard = fakeCards.firstOrNull { it.cardToken == cardId }
                ?: return@withLock NetworkResult.Error("Kart bulunamadi")
            fakeCards.replaceAll { it.copy(isDefault = it.cardToken == cardId) }
            NetworkResult.Success(selectedCard.copy(isDefault = true))
        }
    }

    override suspend fun deleteCard(cardId: String): NetworkResult<Unit> {
        delay(400)
        return mutex.withLock {
            val removed = fakeCards.removeIf { it.cardToken == cardId }
            if (removed) NetworkResult.Success(Unit) else NetworkResult.Error("Kart bulunamadi")
        }
    }
}
