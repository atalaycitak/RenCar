package com.example.rencar_pair.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object OnboardingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object LicenseCheckRoute

@Serializable
data object HomeMapRoute

@Serializable
data class VehicleDetailRoute(val vehicleId: String)

@Serializable
data class ReservationRoute(val vehicleId: String)

@Serializable
data class DeliveryChecklistRoute(
    val rentalId: String,
    val vehicleId: String
)
