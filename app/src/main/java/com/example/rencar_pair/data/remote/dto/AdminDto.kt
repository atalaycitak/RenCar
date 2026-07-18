package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RejectLicenseRequest(
    val reason: String
)

@Serializable
data class AdminLicenseResponse(
    val id: String,
    val userId: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String,
    val selfieImageUrl: String? = null,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val user: AuthUserResponse
)

@Serializable
data class CreateVehicleRequest(
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val latitude: Double,
    val longitude: Double,
    val status: String? = null
)

@Serializable
data class UpdateVehicleRequest(
    val plate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val type: String? = null,
    val pricePerDay: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null
)

/** Kullanıcı özeti — AdminRentalResponseDto.user içinde kullanılır. */
@Serializable
data class RentalUserSummaryResponse(
    val id: String,
    val email: String,
    val fullName: String
)

/** Araç özeti — AdminRentalResponseDto.vehicle içinde kullanılır. */
@Serializable
data class RentalVehicleSummaryAdminResponse(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val status: String
)

/**
 * Admin kiralama listesi ve detay yanıtı.
 *
 * Not: startDate alanı deprecated (eski alan adı). startedAt kullanılmalıdır.
 */
@Serializable
data class AdminRentalResponse(
    val id: String,
    /** Kiralama planı: PER_MINUTE | HOURLY | DAILY */
    val plan: String? = null,
    /** Yolculuğun gerçek başlangıç anı. */
    val startedAt: String? = null,
    /** Yolculuğun gerçek bitiş anı; sürerken null. */
    val endedAt: String? = null,
    /** Planlanan iade tarihi — yalnız DAILY planda dolu. */
    val endDate: String? = null,
    val totalPrice: Double? = null,
    /** Açılış ücreti. */
    val startFee: Double? = null,
    /** Servis ücreti; DAILY'de null. */
    val serviceFee: Double? = null,
    /** Biriken mesafe (km). */
    val distanceKm: Double? = null,
    /** Yolculuk süresi (dakika). */
    val durationMinutes: Double? = null,
    /** PREPARING | ACTIVE | COMPLETED | CANCELLED */
    val status: String,
    /** UNPAID | PAID */
    val paymentStatus: String? = null,
    /** WALLET | CARD | IYZICO */
    val paymentMethod: String? = null,
    val discountAmount: Double? = null,
    val createdAt: String,
    /** @deprecated startedAt kullanın. Geriye uyum için korunuyor. */
    val startDate: String? = null,
    val user: RentalUserSummaryResponse,
    val vehicle: RentalVehicleSummaryAdminResponse
)

@Serializable
data class VehiclePositionResponse(
    val vehicleId: String,
    val plate: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: String
)
