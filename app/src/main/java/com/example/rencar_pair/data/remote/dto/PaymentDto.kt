package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProcessPaymentRequest(
    val method: String,
    val cardId: String? = null,
    val discountCode: String? = null,
    val iyzicoPaymentId: String? = null
)

/**
 * Kullanılan kart özeti — CARD yöntemiyle ödeme yapıldığında PayRentalResponse.card içinde gelir.
 */
@Serializable
data class PaidCardSummaryResponse(
    val id: String? = null,
    val brand: String? = null,
    val last4: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null
)

/**
 * POST /rentals/{id}/pay yanıtı — ödeme makbuzu.
 */
@Serializable
data class ProcessPaymentResponse(
    val rentalId: String? = null,
    /** UNPAID | PAID */
    val paymentStatus: String? = null,
    /** WALLET | CARD | IYZICO */
    val method: String? = null,
    /** Yolculuğun kilitli toplam ücreti (indirim öncesi). */
    val totalPrice: Double? = null,
    /** Uygulanan indirim (TL); kod yoksa 0. */
    val discountAmount: Double? = null,
    /** Fiilen ödenen tutar: max(0, totalPrice - discountAmount). */
    val paidAmount: Double? = null,
    /** Ödeme sonrası cüzdan bakiyesi — yalnız WALLET yönteminde. */
    val walletBalance: Double? = null,
    /** Kullanılan kart özeti — yalnız CARD yönteminde. */
    val card: PaidCardSummaryResponse? = null
)

/**
 * POST /cards request body.
 * Tam kart numarası/CVV GÖNDERILMEZ — yalnızca marka, son 4 hane ve SKT.
 */
@Serializable
data class AddCardRequest(
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int
)

/**
 * GET /cards ve POST /cards yanıtı.
 * API'nin CardResponseDto'suyla birebir eşleşir.
 */
@Serializable
data class CardResponse(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean = false,
    val createdAt: String? = null
)

/**
 * @deprecated CardResponse kullanın. Geriye uyum için korunuyor.
 * Eski Iyzico token alanları (cardToken, cardAlias, binNumber, cardAssociation)
 * /cards endpoint'i tarafından DÖNMEZ.
 */
@Serializable
data class IyzicoCardTokenResponse(
    val id: String? = null,
    val brand: String? = null,
    val last4: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val isDefault: Boolean? = null,
    val createdAt: String? = null
)
