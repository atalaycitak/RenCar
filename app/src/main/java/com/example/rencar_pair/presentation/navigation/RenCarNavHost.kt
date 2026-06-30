package com.example.rencar_pair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rencar_pair.presentation.ui.screens.LoginScreen
import com.example.rencar_pair.presentation.ui.screens.OnboardingScreen
import com.example.rencar_pair.presentation.ui.screens.RegisterScreen
import com.example.rencar_pair.presentation.ui.screens.SplashScreen
import com.example.rencar_pair.presentation.ui.screens.home.HomeRoute
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationRoute

@Composable
fun RenCarNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToLicenseVerification = {
                    navController.navigate(LicenseCheckRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingRoute> {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute)
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onNavigateToHomeMap = {
                    navController.navigate(LicenseCheckRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<LicenseCheckRoute> {
            LicenseVerificationRoute(
                onContinueToMap = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo(LicenseCheckRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<HomeMapRoute> {
            HomeRoute()
        }
    }
}
