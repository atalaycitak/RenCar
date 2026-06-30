package com.example.rencar_pair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.rencar_pair.presentation.ui.screens.HomeMapScreen
import com.example.rencar_pair.presentation.ui.screens.LoginScreen
import com.example.rencar_pair.presentation.ui.screens.OnboardingScreen
import com.example.rencar_pair.presentation.ui.screens.RegisterScreen
import com.example.rencar_pair.presentation.ui.screens.SplashScreen

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
                onNavigateToHomeMap = {
                    navController.navigate(HomeMapRoute) {
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
                    navController.navigate(HomeMapRoute) {
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

        composable<HomeMapRoute> {
            HomeMapScreen()
        }
    }
}
