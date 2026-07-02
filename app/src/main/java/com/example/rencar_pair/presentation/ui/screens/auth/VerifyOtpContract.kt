package com.example.rencar_pair.presentation.ui.screens.auth

import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class VerifyOtpState(
    val phone: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface VerifyOtpIntent : MviIntent {
    data class OnCodeChanged(val code: String) : VerifyOtpIntent
    data object OnVerifyClicked : VerifyOtpIntent
}

sealed class VerifyOtpEffect : MviEffect {
    data object NavigateToHome : VerifyOtpEffect()
    data object NavigateToLicenseVerification : VerifyOtpEffect()
    data class ShowError(val message: String) : VerifyOtpEffect()
}
