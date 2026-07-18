package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TopUpWalletRequest(
    val amount: Double
)

/**
 * Tek bir cüzdan işlemi.
 * type: TOPUP | RENTAL_PAYMENT | REFERRAL_BONUS
 * amount: işaretli tutar (TL) — yükleme/bonus pozitif, ödeme negatif
 */
@Serializable
data class WalletTransactionDto(
    val id: String,
    /** TOPUP | RENTAL_PAYMENT | REFERRAL_BONUS */
    val type: String,
    /** İşaretli tutar (TL): yükleme/bonus +, ödeme -. */
    val amount: Double,
    /** Ödeme hareketiyse ilgili kiralamanın id'si. */
    val rentalId: String? = null,
    /** İşlem açıklaması (ör. "Bakiye yükleme"). */
    val description: String? = null,
    val createdAt: String
)

/**
 * GET /wallet ve POST /wallet/topup yanıtı.
 */
@Serializable
data class WalletInfoResponse(
    val id: String? = null,
    val balance: Double? = null,
    /** @deprecated balance kullanın. Geriye uyum için korunuyor. */
    val currentBalance: Double? = null,
    val transactions: List<WalletTransactionDto> = emptyList()
)

/**
 * @deprecated WalletInfoResponse kullanın. Geriye uyum için korunuyor.
 */
@Serializable
data class WalletBalanceResponse(
    val currentBalance: Double? = null,
    val balance: Double? = null
)
