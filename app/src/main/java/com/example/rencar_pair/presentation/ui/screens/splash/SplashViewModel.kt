package com.example.rencar_pair.presentation.ui.screens.splash

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.GetCurrentUserUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class SplashViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
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
            when (getCurrentUserUseCase()) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isCheckingAuth = false) }
                    emitEffect(SplashEffect.NavigateToLicenseVerification)
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isCheckingAuth = false) }
                    emitEffect(SplashEffect.NavigateToOnboarding)
                }
            }
        }
    }
}
