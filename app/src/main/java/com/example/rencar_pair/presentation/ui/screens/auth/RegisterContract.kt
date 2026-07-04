package com.example.rencar_pair.presentation.ui.screens.auth

import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface RegisterIntent : MviIntent {
    data class OnFullNameChanged(val fullName: String) : RegisterIntent
    data class OnEmailChanged(val email: String) : RegisterIntent
    data class OnPhoneChanged(val phone: String) : RegisterIntent
    data class OnPasswordChanged(val password: String) : RegisterIntent
    data object OnRegisterClicked : RegisterIntent
}

sealed class RegisterEffect : MviEffect {
    data object NavigateToLicenseVerification : RegisterEffect()
}
