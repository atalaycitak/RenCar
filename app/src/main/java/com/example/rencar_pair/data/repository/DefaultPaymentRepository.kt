package com.example.rencar_pair.data.repository

import com.example.rencar_pair.BuildConfig
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.AddCardRequest
import com.example.rencar_pair.data.remote.dto.CardResponse
import com.example.rencar_pair.data.remote.dto.PaidCardSummaryResponse
import com.example.rencar_pair.data.remote.dto.ProcessPaymentRequest
import com.example.rencar_pair.data.remote.dto.ProcessPaymentResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.PaidCardSummary
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentResult
import com.example.rencar_pair.domain.model.PaymentStatus
import com.example.rencar_pair.domain.model.SavedCard
import com.example.rencar_pair.domain.repository.PaymentRepository

class DefaultPaymentRepository(
    private val api: RenCarApi
) : PaymentRepository {
    private val endpointFallback = FakePaymentRepository()

    override suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String?,
        discountCode: String?,
        iyzicoPaymentId: String?
    ): NetworkResult<PaymentResult> {
        if (method == PaymentMethod.Card && cardId.isNullOrBlank()) {
            return NetworkResult.Error("Kart seçimi gerekli")
        }
        if (method == PaymentMethod.Iyzico && iyzicoPaymentId.isNullOrBlank()) {
            return NetworkResult.Error("Iyzico ödeme bilgisi gerekli")
        }

        val result = safeApiCall(
            call = {
                api.processPayment(
                    id = rentalId,
                    request = ProcessPaymentRequest(
                        method = method.toApiValue(),
                        cardId = cardId.takeIf { method == PaymentMethod.Card },
                        discountCode = discountCode,
                        iyzicoPaymentId = iyzicoPaymentId.takeIf { method == PaymentMethod.Iyzico }
                    )
                )
            },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.payRental(
                rentalId = rentalId,
                method = method,
                cardId = cardId,
                discountCode = discountCode,
                iyzicoPaymentId = iyzicoPaymentId
            )
        }
    }

    override suspend fun addCard(
        cardNumber: String,
        expireMonth: String,
        expireYear: String,
        cvc: String,
        cardHolderName: String
    ): NetworkResult<SavedCard> {
        if (cardNumber.length < 16) {
            return NetworkResult.Error("Geçersiz kart numarası")
        }
        val expMonth = expireMonth.toIntOrNull()
            ?: return NetworkResult.Error("Geçersiz son kullanma ayı")
        val expYear = expireYear.toIntOrNull()
            ?: return NetworkResult.Error("Geçersiz son kullanma yılı")

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

    override suspend fun getSavedCards(): NetworkResult<List<SavedCard>> {
        val result = safeApiCall(
            call = { api.getPaymentCards() },
            transform = { cards -> cards.orEmpty().map { it.toDomain() } }
        )
        return result.withEndpointFallback {
            endpointFallback.getSavedCards()
        }
    }

    override suspend fun setDefaultCard(cardId: String): NetworkResult<SavedCard> {
        val result = safeApiCall(
            call = { api.setDefaultCard(cardId) },
            transform = { it.toDomain() }
        )
        return result.withEndpointFallback {
            endpointFallback.setDefaultCard(cardId)
        }
    }

    override suspend fun deleteCard(cardId: String): NetworkResult<Unit> {
        val result = try {
            val response = api.deleteCard(cardId)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("HTTP ${response.code()} error", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error("Kart silinemedi: ${e.localizedMessage}")
        }
        return result.withEndpointFallback {
            endpointFallback.deleteCard(cardId)
        }
    }

    private fun ProcessPaymentResponse.toDomain(): PaymentResult {
        return PaymentResult(
            rentalId = rentalId,
            paymentStatus = PaymentStatus.fromApiString(paymentStatus),
            method = PaymentMethod.fromApiString(method),
            totalPrice = totalPrice,
            discountAmount = discountAmount ?: 0.0,
            paidAmount = paidAmount,
            walletBalance = walletBalance,
            card = card?.toDomain()
        )
    }

    private fun PaidCardSummaryResponse.toDomain(): PaidCardSummary? {
        val resolvedLast4 = last4 ?: return null
        return PaidCardSummary(
            id = id.orEmpty(),
            brand = brand.orEmpty(),
            last4 = resolvedLast4,
            expMonth = expMonth,
            expYear = expYear
        )
    }

    private fun CardResponse.toDomain(): SavedCard {
        val resolvedBrand = brand.ifBlank { "CARD" }
        val resolvedLast4 = last4.ifBlank { "0000" }
        return SavedCard(
            cardToken = id,
            cardAlias = "$resolvedBrand $resolvedLast4".trim(),
            binNumber = resolvedLast4,
            cardAssociation = resolvedBrand,
            expMonth = expMonth,
            expYear = expYear,
            isDefault = isDefault
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
