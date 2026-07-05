package com.example.rencar_pair.presentation.ui.screens.profile

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.AuthUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class ProfileViewModel(
    private val authUseCases: AuthUseCases
) : BaseMviViewModel<ProfileState, ProfileIntent, ProfileEffect>(ProfileState()) {

    init {
        onIntent(ProfileIntent.LoadProfile)
    }

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfile()
            ProfileIntent.Logout -> logout()
        }
    }

    private fun loadProfile() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authUseCases.getCurrentUser()) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false, user = result.data) }
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun logout() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authUseCases.logout()) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(ProfileEffect.NavigateToLogin)
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                    emitEffect(ProfileEffect.ShowError(result.message))
                }
            }
        }
    }
}
