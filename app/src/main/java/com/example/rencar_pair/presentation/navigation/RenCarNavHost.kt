package com.example.rencar_pair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.presentation.ui.screens.LoginScreen
import com.example.rencar_pair.presentation.ui.screens.VerifyOtpScreen
import com.example.rencar_pair.presentation.ui.screens.OnboardingScreen
import com.example.rencar_pair.presentation.ui.screens.RegisterScreen
import com.example.rencar_pair.presentation.ui.screens.SplashScreen
import com.example.rencar_pair.presentation.ui.screens.delivery.DeliveryChecklistRoute as DeliveryChecklistScreenRoute
import com.example.rencar_pair.presentation.ui.screens.home.HomeRoute
import com.example.rencar_pair.presentation.ui.screens.license.LicenseVerificationRoute
import com.example.rencar_pair.presentation.ui.screens.reservation.ReservationRoute as ReservationScreenRoute
import com.example.rencar_pair.presentation.ui.screens.vehicle.VehicleDetailRoute as VehicleDetailScreenRoute
import com.example.rencar_pair.presentation.ui.screens.active_rental.ActiveRentalRoute as ActiveRentalScreenRoute
import com.example.rencar_pair.presentation.ui.screens.trip_summary.TripSummaryRoute as TripSummaryScreenRoute
import com.example.rencar_pair.presentation.ui.screens.wallet.WalletRoute as WalletScreenRoute
import org.koin.compose.koinInject

@Composable
fun RenCarNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val dataStoreManager: DataStoreManager = koinInject()

    LaunchedEffect(dataStoreManager) {
        dataStoreManager.tokenExpired.collect {
            navController.navigate(LoginRoute) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
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
                onNavigateToVerifyOtp = { phone ->
                    navController.navigate(VerifyOtpRoute(phone))
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                }
            )
        }

        composable<VerifyOtpRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VerifyOtpRoute>()
            // SavedStateHandle should automatically have 'phone' from the route
            VerifyOtpScreen(
                onNavigateToHomeMap = {
                    navController.navigate(LicenseCheckRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                        popUpTo(OnboardingRoute) { inclusive = true }
                        popUpTo(VerifyOtpRoute::class.java.name) { inclusive = true }
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
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
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
            HomeRoute(
                onVehicleDetails = { vehicleId ->
                    navController.navigate(VehicleDetailRoute(vehicleId))
                }
            )
        }

        composable<VehicleDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VehicleDetailRoute>()
            VehicleDetailScreenRoute(
                vehicleId = route.vehicleId,
                onBack = { navController.popBackStack() },
                onReserve = { vehicleId ->
                    navController.navigate(ReservationRoute(vehicleId))
                }
            )
        }

        composable<ReservationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReservationRoute>()
            ReservationScreenRoute(
                vehicleId = route.vehicleId,
                onBack = { navController.popBackStack() },
                onDeliveryChecklist = { rentalId, vehicleId ->
                    navController.navigate(DeliveryChecklistRoute(rentalId, vehicleId))
                }
            )
        }

        composable<DeliveryChecklistRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DeliveryChecklistRoute>()
            DeliveryChecklistScreenRoute(
                rentalId = route.rentalId,
                vehicleId = route.vehicleId,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(ActiveRentalRoute(route.rentalId)) {
                        popUpTo(HomeMapRoute) { inclusive = false }
                    }
                }
            )
        }

        composable<ActiveRentalRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ActiveRentalRoute>()
            ActiveRentalScreenRoute(
                rentalId = route.rentalId,
                onNavigateToSummary = { rentalId ->
                    navController.navigate(TripSummaryRoute(rentalId)) {
                        popUpTo(ActiveRentalRoute(rentalId)) { inclusive = true }
                    }
                }
            )
        }

        composable<TripSummaryRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TripSummaryRoute>()
            TripSummaryScreenRoute(
                rentalId = route.rentalId,
                onNavigateToHome = {
                    navController.navigate(HomeMapRoute) {
                        popUpTo(HomeMapRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<WalletRoute> {
            WalletScreenRoute()
        }
    }
}
