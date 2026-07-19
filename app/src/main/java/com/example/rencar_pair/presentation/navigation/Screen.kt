package com.example.rencar_pair.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object OnboardingRoute

@Serializable
data class LoginRoute(val sessionExpired: Boolean = false)

@Serializable
data class ReturnVehicleRoute(val rentalId: String)

@Serializable
data class VerifyOtpRoute(val phone: String)

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

@Serializable
data class ActiveRentalRoute(val rentalId: String)

@Serializable
data class TripSummaryRoute(val rentalId: String)

@Serializable
data object WalletRoute

@Serializable
data object TripHistoryListRoute

@Serializable
data object ProfileRoute

@Serializable
data object SettingsRoute

@Serializable
data object ReferralRoute
