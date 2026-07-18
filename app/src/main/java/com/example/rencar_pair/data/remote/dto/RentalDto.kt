package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRentalRequest(
    val vehicleId: String,
    val plan: String? = null,
    val endDate: String? = null
)

/**
 * Kiralama yanıtında gelen araç özeti.
 * Tam VehicleResponse yerine yalnızca temel bilgileri içerir.
 */
@Serializable
data class RentalVehicleSummaryResponse(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String
)

/**
 * POST /rentals, GET /rentals, GET /rentals/{id}, POST /rentals/{id}/start,
 * POST /rentals/{id}/return yanıtı.
 *
 * Not: startDate deprecated olup startedAt kullanılmalıdır.
 */
@Serializable
data class RentalResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    /** Araç özeti — v2 API'de dolu, eski sürümlerde null olabilir. */
    val vehicle: RentalVehicleSummaryResponse? = null,
    /** Kiralama planı: PER_MINUTE, HOURLY, DAILY */
    val plan: String? = null,
    /** Yolculuğun gerçek başlangıç anı. */
    val startedAt: String? = null,
    /** Yolculuğun gerçek bitiş anı; sürerken null. */
    val endedAt: String? = null,
    /** Planlanan iade tarihi — yalnız DAILY planda dolu. */
    val endDate: String? = null,
    /** Toplam ücret. DAILY'de oluştururken kilitlenir; dk/sa planında finish sonrası dolu. */
    val totalPrice: Double? = null,
    /** Açılış ücreti (oluştururken kilitlenir; DAILY'de 0). */
    val startFee: Double? = null,
    /** Servis ücreti (finish'te hesaplanır; DAILY'de null). */
    val serviceFee: Double? = null,
    /** Yolculuk boyunca biriken mesafe (km). */
    val distanceKm: Double? = null,
    /** Yolculuk süresi (dakika, yukarı yuvarlı). */
    val durationMinutes: Double? = null,
    /** PREPARING | ACTIVE | COMPLETED | CANCELLED */
    val status: String,
    /** UNPAID | PAID */
    val paymentStatus: String? = null,
    /** Ödeme yöntemi — yalnız ödeme alındıysa dolu: WALLET | CARD | IYZICO */
    val paymentMethod: String? = null,
    /** Ödemede uygulanan indirim (TL); kod kullanılmadıysa 0. */
    val discountAmount: Double? = null,
    val createdAt: String,
    /** @deprecated startedAt kullanın. Geriye uyum için korunuyor. */
    val startDate: String? = null
)

/**
 * GET /rentals/active yanıtı — RentalResponse alanlarının tamamına ek olarak
 * anlık hesaplanan elapsedSeconds ve currentCost içerir.
 */
@Serializable
data class ActiveRentalResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryResponse? = null,
    val plan: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val endDate: String? = null,
    val totalPrice: Double? = null,
    val startFee: Double? = null,
    val serviceFee: Double? = null,
    val distanceKm: Double? = null,
    val durationMinutes: Double? = null,
    val status: String,
    val paymentStatus: String? = null,
    val paymentMethod: String? = null,
    val discountAmount: Double? = null,
    val createdAt: String,
    /** Yolculuk başlangıcından bu yana geçen süre (saniye). */
    val elapsedSeconds: Double? = null,
    /** Yolculuk şu an bitirilse ödenecek tahmini tutar. */
    val currentCost: Double? = null,
    /** @deprecated */
    val startDate: String? = null
)

/**
 * POST /rentals/{id}/finish yanıtı — RentalResponse alanlarına ek olarak
 * usageFee (kullanım ücreti kalemi) ve elapsedSeconds (toplam süre) içerir.
 */
@Serializable
data class FinishRentalResponse(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryResponse? = null,
    val plan: String? = null,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val endDate: String? = null,
    val totalPrice: Double? = null,
    val startFee: Double? = null,
    val serviceFee: Double? = null,
    val distanceKm: Double? = null,
    val durationMinutes: Double? = null,
    val status: String,
    val paymentStatus: String? = null,
    val paymentMethod: String? = null,
    val discountAmount: Double? = null,
    val createdAt: String,
    /** Kullanım ücreti kalemi (PER_MINUTE/HOURLY/DAILY formülüne göre). */
    val usageFee: Double? = null,
    /** Yolculuğun toplam süresi (saniye). */
    val elapsedSeconds: Double? = null,
    /** @deprecated */
    val startDate: String? = null
)
