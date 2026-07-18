package com.example.rencar_pair.domain.location

import com.example.rencar_pair.domain.model.VehiclePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculates the distance between two VehiclePoints in kilometers using the Haversine formula.
 */
fun VehiclePoint.distanceTo(other: VehiclePoint): Double {
    val earthRadiusKm = 6371.0

    val dLat = Math.toRadians(other.latitude - this.latitude)
    val dLon = Math.toRadians(other.longitude - this.longitude)

    val lat1 = Math.toRadians(this.latitude)
    val lat2 = Math.toRadians(other.latitude)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusKm * c
}
