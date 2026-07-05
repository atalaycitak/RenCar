package com.example.rencar_pair.presentation.ui.screens.auth

import androidx.compose.runtime.Stable
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class LoginState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface LoginIntent : MviIntent {
    data class OnPhoneChanged(val phone: String) : LoginIntent
    data object OnLoginClicked : LoginIntent
}

sealed interface LoginEffect : MviEffect {
    data class NavigateToVerifyOtp(val phone: String) : LoginEffect
}
