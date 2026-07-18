package com.example.rencar_pair.data.repository

import com.example.rencar_pair.BuildConfig
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.AddCardRequest
import com.example.rencar_pair.data.remote.dto.CardResponse
import com.example.rencar_pair.data.remote.dto.ProcessPaymentRequest
import com.example.rencar_pair.data.remote.dto.ProcessPaymentResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.PaymentStatus
import com.example.rencar_pair.domain.repository.PaymentRepository

class DefaultPaymentRepository(
    private val api: RenCarApi
) : PaymentRepository {
    private val endpointFallback = FakePaymentRepository()

    override suspend fun processPayment(
        rentalId: String,
        cardToken: String,
        amount: Double
    ): NetworkResult<PaymentResult> {
        if (amount <= 0) {
            return NetworkResult.Error("Gecersiz tutar")
        }
        val result = safeApiCall(
            call = {
                api.processPayment(
                    id = rentalId,
                    request = ProcessPaymentRequest(
                        method = "CARD",
                        cardId = cardToken
                    )
                )
            },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.processPayment(rentalId, cardToken, amount)
        }
    }

    override suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<PaymentMethod> {
        if (cardNumber.length < 16) {
            return NetworkResult.Error("Gecersiz kart numarasi")
        }
        val expMonth = expireMonth.toIntOrNull()
            ?: return NetworkResult.Error("Gecersiz son kullanma ayi")
        val expYear = expireYear.toIntOrNull()
            ?: return NetworkResult.Error("Gecersiz son kullanma yili")

        val result = safeApiCall(
            call = {
                api.addPaymentCard(
                    AddCardRequest(
                        brand = cardNumber.toCardBrand(),
                        last4 = cardNumber.takeLast(4),
                        expMonth = expMonth,
                        expYear = expYear
                    )
                )
            },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.addCard(cardNumber, expireMonth, expireYear, cvc, cardHolderName)
        }
    }

    override suspend fun getSavedCards(): NetworkResult<List<PaymentMethod>> {
        val result = safeApiCall(
            call = { api.getPaymentCards() },
            transform = { cards -> cards.orEmpty().map { it.toDomain() } }
        )
        return result.withEndpointFallback {
            endpointFallback.getSavedCards()
        }
    }

    private fun ProcessPaymentResponse.toDomain(): PaymentResult {
        return PaymentResult(
            status = PaymentStatus.fromApiString(paymentStatus),
            transactionId = null,
            errorMessage = null
        )
    }

    private fun CardResponse.toDomain(): PaymentMethod {
        val resolvedBrand = brand
        val resolvedLast4 = last4
        return PaymentMethod(
            cardToken = id,
            cardAlias = "$resolvedBrand $resolvedLast4".trim(),
            binNumber = resolvedLast4,
            cardAssociation = resolvedBrand
        )
    }

    private fun String.toCardBrand(): String {
        return if (startsWith("4")) "VISA" else "MASTERCARD"
    }

    private suspend fun <T> NetworkResult<T>.withEndpointFallback(
        fallback: suspend () -> NetworkResult<T>
    ): NetworkResult<T> {
        return if (BuildConfig.DEBUG && this is NetworkResult.Error && code in ENDPOINT_NOT_READY_CODES) {
            fallback()
        } else {
            this
        }
    }

    private companion object {
        val ENDPOINT_NOT_READY_CODES = setOf(404, 405, 501)
    }
}
