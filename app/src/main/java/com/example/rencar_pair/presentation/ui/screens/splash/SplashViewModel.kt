package com.example.rencar_pair.presentation.ui.screens.splash

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.AuthUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class SplashViewModel(
    private val authUseCases: AuthUseCases
) : BaseMviViewModel<SplashState, SplashIntent, SplashEffect>(SplashState()) {

    init {
        onIntent(SplashIntent.CheckAuth)
    }

    override fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.CheckAuth -> checkAuth()
        }
    }

    private fun checkAuth() {
        launchCoroutine {
            when (val result = authUseCases.getCurrentUser()) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isCheckingAuth = false) }
                    val user = result.data
                    if (user.role == com.example.rencar_pair.domain.model.UserRole.Pending) {
                        emitEffect(SplashEffect.NavigateToLicenseVerification)
                    } else {
                        emitEffect(SplashEffect.NavigateToHome)
                    }
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isCheckingAuth = false) }
                    emitEffect(SplashEffect.NavigateToOnboarding)
                }
            }
        }
    }
}
