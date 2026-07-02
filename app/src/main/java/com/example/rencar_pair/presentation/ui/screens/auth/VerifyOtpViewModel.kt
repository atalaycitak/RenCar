package com.example.rencar_pair.presentation.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.VerifyOtpUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VerifyOtpViewModel(
    savedStateHandle: SavedStateHandle,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyOtpState())
    val state: StateFlow<VerifyOtpState> = _state.asStateFlow()

    private val _effect = Channel<VerifyOtpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        val phone = savedStateHandle.get<String>("phone") ?: ""
        _state.value = _state.value.copy(phone = phone)
    }

    fun onIntent(intent: VerifyOtpIntent) {
        when (intent) {
            is VerifyOtpIntent.OnCodeChanged -> {
                _state.value = _state.value.copy(code = intent.code, errorMessage = null)
            }
            is VerifyOtpIntent.OnVerifyClicked -> verify()
        }
    }

    private fun verify() {
        val currentState = _state.value

        if (currentState.code.isBlank() || currentState.code.length != 6) {
            _state.value = currentState.copy(errorMessage = "Geçerli bir 6 haneli kod girin")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = verifyOtpUseCase(currentState.phone, currentState.code)) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(VerifyOtpEffect.NavigateToHome)
                }
                is NetworkResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }
}
