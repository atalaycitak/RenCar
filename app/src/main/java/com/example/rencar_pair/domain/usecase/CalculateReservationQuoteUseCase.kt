package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.model.ReservationQuote
import com.example.rencar_pair.domain.model.Vehicle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class CalculateReservationQuoteUseCase {

    operator fun invoke(vehicle: Vehicle, days: Int): ReservationQuote {
        val safeDays = days.coerceIn(1, 30)
        val subtotal = vehicle.pricePerDay * safeDays
        val serviceFee = roundMoney(subtotal * SERVICE_FEE_RATE)
        val deliveryFee = if (safeDays < FREE_DELIVERY_DAY_THRESHOLD) DELIVERY_FEE else 0.0
        val totalPrice = roundMoney(subtotal + serviceFee + deliveryFee)

        return ReservationQuote(
            vehicleId = vehicle.id,
            days = safeDays,
            pricePerDay = vehicle.pricePerDay,
            serviceFee = serviceFee,
            deliveryFee = deliveryFee,
            totalPrice = totalPrice,
            endDateIso = formatIso(System.currentTimeMillis() + safeDays * ONE_DAY_MS)
        )
    }

    private fun roundMoney(value: Double): Double {
        return (value * 100).roundToInt() / 100.0
    }

    private fun formatIso(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private companion object {
        const val SERVICE_FEE_RATE = 0.08
        const val FREE_DELIVERY_DAY_THRESHOLD = 3
        const val DELIVERY_FEE = 150.0
        const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    }
}
