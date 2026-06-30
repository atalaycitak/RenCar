package com.example.rencar_pair.presentation.mvi

interface MviState

interface MviIntent

sealed class MviEffect {
    data class ShowSnackbar(val message: String) : MviEffect()
    data class Navigate(val route: String) : MviEffect()
    data object NavigateBack : MviEffect()
}
