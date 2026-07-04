package com.example.rencar_pair.presentation.ui.screens.profile

import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface ProfileIntent : MviIntent {
    data object LoadProfile : ProfileIntent
    data object Logout : ProfileIntent
}

sealed interface ProfileEffect : MviEffect {
    data object NavigateToLogin : ProfileEffect
    data class ShowError(val message: String) : ProfileEffect
}
