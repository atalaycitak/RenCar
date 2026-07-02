package com.example.rencar_pair.presentation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.usecase.RegisterUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.OnFullNameChanged -> {
                _state.value = _state.value.copy(fullName = intent.fullName, errorMessage = null)
            }
            is RegisterIntent.OnEmailChanged -> {
                _state.value = _state.value.copy(email = intent.email, errorMessage = null)
            }
            is RegisterIntent.OnPhoneChanged -> {
                _state.value = _state.value.copy(phone = intent.phone, errorMessage = null)
            }
            is RegisterIntent.OnPasswordChanged -> {
                _state.value = _state.value.copy(password = intent.password, errorMessage = null)
            }
            is RegisterIntent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val currentState = _state.value

        if (currentState.fullName.isBlank() || currentState.email.isBlank() || currentState.phone.isBlank() || currentState.password.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Lütfen tüm alanları doldurun")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            when (val result = registerUseCase(currentState.fullName, currentState.email, currentState.phone, currentState.password)) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _effect.send(RegisterEffect.NavigateToLicenseVerification)
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
