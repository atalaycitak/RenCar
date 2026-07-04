package com.example.rencar_pair.presentation.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.VerifyOtpUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class VerifyOtpViewModel(
    savedStateHandle: SavedStateHandle,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : BaseMviViewModel<VerifyOtpState, VerifyOtpIntent, VerifyOtpEffect>(VerifyOtpState()) {

    init {
        val phone = savedStateHandle.get<String>("phone") ?: ""
        updateState { it.copy(phone = phone) }
    }

    override fun onIntent(intent: VerifyOtpIntent) {
        when (intent) {
            is VerifyOtpIntent.OnCodeChanged -> updateState {
                it.copy(code = intent.code, errorMessage = null)
            }
            is VerifyOtpIntent.OnVerifyClicked -> verify()
        }
    }

    private fun verify() {
        val current = currentState()

        if (current.code.isBlank() || current.code.length != 6) {
            updateState { it.copy(errorMessage = "Geçerli bir 6 haneli kod girin") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            when (val result = verifyOtpUseCase(current.phone, current.code)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(VerifyOtpEffect.NavigateToHome)
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
