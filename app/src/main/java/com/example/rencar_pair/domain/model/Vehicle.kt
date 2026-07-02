package com.example.rencar_pair.domain.model

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val rangeKm: Int = 320,
    val locationName: String = "Istanbul"
) {
    val title: String = "$brand $model"
}
