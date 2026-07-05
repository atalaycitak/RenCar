package com.example.rencar_pair.presentation.ui.screens.splash

import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class SplashState(
    val isCheckingAuth: Boolean = true
) : MviState

sealed interface SplashIntent : MviIntent {
    data object CheckAuth : SplashIntent
}

sealed class SplashEffect : MviEffect {
    data object NavigateToOnboarding : SplashEffect()
    data object NavigateToLicenseVerification : SplashEffect()
}
