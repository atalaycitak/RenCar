package com.example.rencar_pair.presentation.ui.screens.splash

import androidx.compose.runtime.Stable
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class SplashState(
    val isCheckingAuth: Boolean = true
) : MviState

sealed interface SplashIntent : MviIntent {
    data object CheckAuth : SplashIntent
}

sealed interface SplashEffect : MviEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToLicenseVerification : SplashEffect
    data object NavigateToHome : SplashEffect
}
