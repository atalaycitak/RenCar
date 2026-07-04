package com.example.rencar_pair.presentation.ui.screens.profile

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.GetCurrentUserUseCase
import com.example.rencar_pair.domain.usecase.LogoutUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
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
            when (val result = getCurrentUserUseCase()) {
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
            when (val result = logoutUseCase()) {
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
