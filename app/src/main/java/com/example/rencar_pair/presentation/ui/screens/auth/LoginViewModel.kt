package com.example.rencar_pair.presentation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.usecase.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnEmailChanged -> {
                _state.value = _state.value.copy(email = intent.email, errorMessage = null)
            }
            is LoginIntent.OnPasswordChanged -> {
                _state.value = _state.value.copy(password = intent.password, errorMessage = null)
            }
            is LoginIntent.OnLoginClicked -> login()
        }
    }

    private fun login() {
        val currentState = _state.value

        if (currentState.email.isBlank()) {
            _state.value = currentState.copy(errorMessage = "E-posta adresi gerekli")
            return
        }
        if (currentState.password.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Şifre gerekli")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = loginUseCase(currentState.email, currentState.password)) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(LoginEffect.NavigateToHome)
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
