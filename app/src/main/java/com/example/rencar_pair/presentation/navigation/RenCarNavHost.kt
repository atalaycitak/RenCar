package com.example.rencar_pair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rencar_pair.presentation.ui.screens.SplashScreen
import com.example.rencar_pair.presentation.ui.screens.home.HomeRoute
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationRoute

@Composable
fun RenCarNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RenCarRoute.Splash.value
    ) {
        composable(RenCarRoute.Splash.value) {
            SplashScreen(
                onContinue = {
                    navController.navigate(RenCarRoute.LicenseVerification.value) {
                        popUpTo(RenCarRoute.Splash.value) { inclusive = true }
                    }
                }
            )
        }
        composable(RenCarRoute.LicenseVerification.value) {
            LicenseVerificationRoute(
                onContinueToMap = {
                    navController.navigate(RenCarRoute.Home.value) {
                        popUpTo(RenCarRoute.LicenseVerification.value) { inclusive = true }
                    }
                }
            )
        }
        composable(RenCarRoute.Home.value) {
            HomeRoute()
        }
    }
}
