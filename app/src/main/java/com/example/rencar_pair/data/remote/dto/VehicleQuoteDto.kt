package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * GET /vehicles/{id}/quote yanıtı — fiyat önizleme.
 * Hiçbir kayıt oluşturmaz, aracı kilitlemez. Salt hesap.
 */
@Serializable
data class QuoteResponse(
    val vehicleId: String,
    /** PER_MINUTE | HOURLY | DAILY */
    val plan: String,
    /** Sorgulanan süre (dakika). */
    val minutes: Double,
    /** Kullanım ücreti kalemi. */
    val usageFee: Double,
    /** Açılış ücreti (DAILY planda 0). */
    val startFee: Double,
    /** Servis ücreti (kullanımın yüzdesi; DAILY planda 0). */
    val serviceFee: Double,
    /** Tahmini toplam = usageFee + startFee + serviceFee. */
    val estimatedTotal: Double
)

/**
 * GET /rentals/stats yanıtı — aylık yolculuk özeti.
 * Yalnızca TAMAMLANMIŞ (COMPLETED) yolculuklar dahil edilir.
 * Boş ay sıfırlarla döner.
 */
@Serializable
data class RentalStatsResponse(
    /** İstatistiğin ayı (YYYY-MM, UTC). */
    val month: String,
    /** Ay içinde tamamlanan yolculuk sayısı. */
    val tripCount: Double,
    /** Toplam harcama (TL) — yalnız PAID yolculukların indirim sonrası tutarı. */
    val totalSpent: Double,
    /** Toplam yolculuk süresi (dakika). */
    val totalMinutes: Double,
    /** Toplam mesafe (km, simülasyon telemetrisi). */
    val totalKm: Double
)
