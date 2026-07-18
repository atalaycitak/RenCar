package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────
// REQUEST DTO'LARI
// ─────────────────────────────────────────────

/**
 * POST /iyzico/payments ve POST /iyzico/payments/threeds/initialize request body.
 * Kart bilgisi (card) ile doğrudan ödeme veya 3D Secure başlatma için kullanılır.
 */
@Serializable
data class CreateIyzicoPaymentRequest(
    /** Tahsil edilecek tutar (TL). */
    val price: Double,
    /** Taksit sayısı (1-12). Varsayılan: 1. */
    val installment: Int = 1,
    /** Sepet kaleminde görünecek açıklama. */
    val description: String? = null,
    /**
     * Sepet ID. Kiralama ödemesi için `rental-<kiralamaId>` formatında gönderin.
     * POST /rentals/:id/pay (method: IYZICO) bu ID'yi doğrular.
     */
    val basketId: String? = null,
    /** Kart bilgisi. */
    val card: IyzicoCardRequest,
    /** Alıcı bilgisi geçersiz kılmaları (opsiyonel). */
    val buyer: IyzicoBuyerRequest? = null
)

/**
 * POST /iyzico/checkout-form/initialize request body.
 * Iyzico Checkout Form (WebView) akışını başlatır.
 */
@Serializable
data class InitializeCheckoutFormRequest(
    /** Tahsil edilecek tutar (TL). */
    val price: Double,
    /** Sepet kaleminde görünecek açıklama. */
    val description: String? = null,
    /**
     * Sepet ID. Kiralama ödemesi için `rental-<kiralamaId>` formatında gönderin.
     */
    val basketId: String? = null,
    /** Formda sunulacak taksit seçenekleri. Varsayılan: [1] (tek çekim). */
    val enabledInstallments: List<Int>? = null,
    /** Alıcı bilgisi geçersiz kılmaları (opsiyonel). */
    val buyer: IyzicoBuyerRequest? = null
)

/**
 * POST /iyzico/payments/{paymentId}/cancel request body.
 */
@Serializable
data class CancelIyzicoPaymentRequest(
    /** İptal nedeni: DOUBLE_PAYMENT | BUYER_REQUEST | FRAUD | OTHER */
    val reason: String,
    /** Serbest metin açıklama. */
    val description: String? = null
)

/**
 * POST /iyzico/refunds request body.
 */
@Serializable
data class RefundIyzicoPaymentRequest(
    /** İade edilecek işlem kırılımının ID'si (IyzicoPaymentResponse.paymentTransactionIds listesinden). */
    val paymentTransactionId: String,
    /** İade tutarı (TL). Kalem tutarını aşamaz. */
    val price: Double,
    /** İade nedeni: DOUBLE_PAYMENT | BUYER_REQUEST | FRAUD | OTHER */
    val reason: String,
    /** Serbest metin açıklama. */
    val description: String? = null
)

// ─────────────────────────────────────────────
// İÇ (NESTED) DTO'LAR
// ─────────────────────────────────────────────

/**
 * Iyzico doğrudan ödeme akışında kart bilgisi.
 * NOT: Tam kart numarası yalnızca Iyzico akışında kullanılır;
 * uygulama tarafı bu veriyi Iyzico ile şifreli kanal üzerinden iletir.
 */
@Serializable
data class IyzicoCardRequest(
    val cardHolderName: String,
    val cardNumber: String,
    val expireMonth: String,
    val expireYear: String,
    val cvc: String
)

/**
 * Alıcı (buyer) bilgisi geçersiz kılmaları — opsiyonel.
 * Verilmezse backend sandbox varsayılanlarını kullanır.
 */
@Serializable
data class IyzicoBuyerRequest(
    val identityNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val zipCode: String? = null
)

// ─────────────────────────────────────────────
// RESPONSE DTO'LARI
// ─────────────────────────────────────────────

/**
 * POST /iyzico/payments, POST /iyzico/payments/threeds/initialize,
 * GET /iyzico/payments/{paymentId}, GET /iyzico/checkout-form/result/{token} yanıtı.
 */
@Serializable
data class IyzicoPaymentResponse(
    /** İyzico işlem durumu (success | failure). */
    val status: String? = null,
    /** İyzico ödeme ID — iptal/sorgu bu ID ile yapılır. */
    val paymentId: String? = null,
    /** İstek eşleştirme ID. */
    val conversationId: String? = null,
    /** Sepet tutarı (TL). */
    val price: Double? = null,
    /** Karttan çekilen tutar (TL). */
    val paidPrice: Double? = null,
    /** Para birimi (TRY). */
    val currency: String? = null,
    /** Taksit sayısı. */
    val installment: Int? = null,
    /** Checkout Form sonucundaki durum (SUCCESS | FAILURE | INIT_THREEDS | CALLBACK_THREEDS). */
    val paymentStatus: String? = null,
    /** Checkout Form token'ı (yalnız form akışında dolu). */
    val token: String? = null,
    /** Fraud kontrol durumu: 1=onaylı, 0=incelemede, -1=red. */
    val fraudStatus: Int? = null,
    /** Kartın ilk 6 hanesi. */
    val binNumber: String? = null,
    /** Kartın son 4 hanesi. */
    val lastFourDigits: String? = null,
    /** Kart tipi (örn. CREDIT_CARD). */
    val cardType: String? = null,
    /** Kart şeması (örn. MASTER_CARD, VISA). */
    val cardAssociation: String? = null,
    /** Kart ailesi (örn. Paraf, Bonus). */
    val cardFamily: String? = null,
    /**
     * İade için gereken işlem kırılım ID'leri.
     * POST /iyzico/refunds'a gönderilecek paymentTransactionId değerleri buradan alınır.
     */
    val paymentTransactionIds: List<String> = emptyList()
)

/**
 * POST /iyzico/payments/threeds/initialize yanıtı.
 * threeDSHtmlContentDecoded doğrudan WebView'a yüklenebilir.
 */
@Serializable
data class ThreedsInitializeResponse(
    val status: String? = null,
    val conversationId: String? = null,
    /** Bankanın 3DS doğrulama sayfası (Base64 kodlu). */
    val threeDSHtmlContent: String? = null,
    /** Aynı içeriğin Base64'ten çözülmüş hali — doğrudan WebView'a yüklenebilir. */
    val threeDSHtmlContentDecoded: String? = null
)

/**
 * POST /iyzico/checkout-form/initialize yanıtı.
 * checkoutFormContent WebView'a yüklenecek HTML içeriği;
 * token ise GET /iyzico/checkout-form/result/{token} sorgusunda kullanılır.
 */
@Serializable
data class CheckoutFormInitializeResponse(
    val status: String? = null,
    val token: String? = null,
    val checkoutFormContent: String? = null
)
