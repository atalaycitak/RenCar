package com.example.rencar_pair.presentation.ui.screens.auth

import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface LoginIntent : MviIntent {
    data class OnEmailChanged(val email: String) : LoginIntent
    data class OnPasswordChanged(val password: String) : LoginIntent
    data object OnLoginClicked : LoginIntent
}

sealed class LoginEffect : MviEffect() {
    data object NavigateToHome : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}
