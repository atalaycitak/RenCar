package com.example.rencar_pair.presentation.ui.screens.auth

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : BaseMviViewModel<RegisterState, RegisterIntent, RegisterEffect>(RegisterState()) {

    override fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.OnFullNameChanged -> updateState {
                it.copy(fullName = intent.fullName, errorMessage = null)
            }
            is RegisterIntent.OnEmailChanged -> updateState {
                it.copy(email = intent.email, errorMessage = null)
            }
            is RegisterIntent.OnPhoneChanged -> updateState {
                it.copy(phone = intent.phone, errorMessage = null)
            }
            is RegisterIntent.OnPasswordChanged -> updateState {
                it.copy(password = intent.password, errorMessage = null)
            }
            is RegisterIntent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val current = currentState()

        if (current.fullName.isBlank() || current.email.isBlank() ||
            current.phone.isBlank() || current.password.isBlank()
        ) {
            updateState { it.copy(errorMessage = "Lütfen tüm alanları doldurun") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            val fullPhone = "+90${current.phone}"

            when (val result = registerUseCase(
                current.fullName, current.email, fullPhone, current.password
            )) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(RegisterEffect.NavigateToLicenseVerification)
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
