package com.example.rencar_pair.presentation.ui.screens.auth

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.LoginUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseMviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnPhoneChanged -> updateState {
                it.copy(phone = intent.phone, errorMessage = null)
            }
            is LoginIntent.OnLoginClicked -> login()
        }
    }

    private fun login() {
        val phone = currentState().phone

        if (phone.isBlank()) {
            updateState { it.copy(errorMessage = "Telefon numarası gerekli") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            val fullPhone = "+90$phone"

            when (val result = loginUseCase(fullPhone)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(LoginEffect.NavigateToVerifyOtp(fullPhone))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
