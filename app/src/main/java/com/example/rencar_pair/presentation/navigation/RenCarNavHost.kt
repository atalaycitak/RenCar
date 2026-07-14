package com.example.rencar_pair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.rencar_pair.presentation.ui.screens.auth.LoginScreen
import com.example.rencar_pair.presentation.ui.screens.auth.VerifyOtpScreen
import com.example.rencar_pair.presentation.ui.screens.onboarding.OnboardingScreen
import com.example.rencar_pair.presentation.ui.screens.auth.RegisterScreen
import com.example.rencar_pair.presentation.ui.screens.splash.SplashScreen
import com.example.rencar_pair.presentation.ui.screens.delivery.DeliveryChecklistScreen
import com.example.rencar_pair.presentation.ui.screens.home.HomeScreen
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationScreen
import com.example.rencar_pair.presentation.ui.screens.reservation.ReservationScreen
import com.example.rencar_pair.presentation.ui.screens.vehicle.VehicleDetailScreen
import com.example.rencar_pair.presentation.ui.screens.active_rental.ActiveRentalScreen
import com.example.rencar_pair.presentation.ui.screens.trip_summary.TripSummaryScreen
import com.example.rencar_pair.presentation.ui.screens.wallet.WalletScreen
import com.example.rencar_pair.presentation.ui.screens.history.TripHistoryScreen
import com.example.rencar_pair.presentation.ui.screens.profile.ProfileScreen
import com.example.rencar_pair.presentation.ui.screens.return_vehicle.ReturnVehicleScreen

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
                    navController.navigate(LoginRoute())
                }
            )
        }

        composable<LoginRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LoginRoute>()
            LoginScreen(
                sessionExpired = route.sessionExpired,
                onNavigateToVerifyOtp = { phone ->
                    navController.navigate(VerifyOtpRoute(phone))
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                }
            )
        }

        composable<VerifyOtpRoute> {
            VerifyOtpScreen(
                onNavigateToHomeMap = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                        popUpTo<OnboardingRoute> { inclusive = true }
                        popUpTo<VerifyOtpRoute> { inclusive = true }
                    }
                },
                onNavigateToLicenseVerification = {
                    navController.navigate(LicenseCheckRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                        popUpTo<OnboardingRoute> { inclusive = true }
                        popUpTo<VerifyOtpRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToLicenseVerification = {
                    navController.navigate(LicenseCheckRoute) {
                        popUpTo<RegisterRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<LicenseCheckRoute> {
            LicenseVerificationScreen(
                onContinueToMap = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo<LicenseCheckRoute> { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(LoginRoute()) {
                        popUpTo(LicenseCheckRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<HomeMapRoute> {
            HomeScreen(
                onVehicleDetails = { vehicleId ->
                    navController.navigate(VehicleDetailRoute(vehicleId))
                },
                onNavigateToHistory = {
                    navController.navigate(TripHistoryListRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<VehicleDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VehicleDetailRoute>()
            VehicleDetailScreen(
                onBack = { navController.popBackStack() },
                onReserve = { vehicleId ->
                    navController.navigate(ReservationRoute(vehicleId))
                }
            )
        }

        composable<ReservationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReservationRoute>()
            ReservationScreen(
                onBack = { navController.popBackStack() },
                onDeliveryChecklist = { rentalId, vehicleId ->
                    navController.navigate(DeliveryChecklistRoute(rentalId, vehicleId)) {
                        popUpTo<ReservationRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<DeliveryChecklistRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DeliveryChecklistRoute>()
            DeliveryChecklistScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(ActiveRentalRoute(route.rentalId)) {
                        popUpTo<HomeMapRoute>()
                    }
                }
            )
        }

        composable<ActiveRentalRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ActiveRentalRoute>()
            ActiveRentalScreen(
                rentalId = route.rentalId,
                onNavigateToReturnVehicle = { rentalId ->
                    navController.navigate(ReturnVehicleRoute(rentalId)) {
                        popUpTo<ActiveRentalRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<TripSummaryRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TripSummaryRoute>()
            TripSummaryScreen(
                rentalId = route.rentalId,
                onNavigateToHome = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo<HomeMapRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<WalletRoute> {
            WalletScreen()
        }

        composable<TripHistoryListRoute> {
            TripHistoryScreen(
                onNavigateToHome = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(TripHistoryListRoute) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute()) {
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<ReturnVehicleRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReturnVehicleRoute>()
            ReturnVehicleScreen(
                rentalId = route.rentalId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSummary = { rentalId ->
                    navController.navigate(TripSummaryRoute(rentalId)) {
                        popUpTo<ReturnVehicleRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
