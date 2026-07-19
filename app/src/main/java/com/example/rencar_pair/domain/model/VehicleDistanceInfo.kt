package com.example.rencar_pair.domain.model

import kotlin.math.roundToInt

data class VehicleDistanceInfo(
    val distanceKm: Double,
    val walkingMinutes: Int
) {
    val distanceLabel: String
        get() = if (distanceKm < 1.0) {
            "${(distanceKm * 1000).roundToInt()} m"
        } else {
            "${(distanceKm * 10).roundToInt() / 10.0} km"
        }
}
