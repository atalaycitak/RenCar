package com.example.rencar_pair.presentation.navigation

sealed class RenCarRoute(val value: String) {
    data object Splash : RenCarRoute("splash")
    data object LicenseVerification : RenCarRoute("license_verification")
    data object Home : RenCarRoute("home")
}
