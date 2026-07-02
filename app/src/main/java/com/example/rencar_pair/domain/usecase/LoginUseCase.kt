package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phone: String): NetworkResult<String> {
        if (phone.isBlank()) {
            return NetworkResult.Error("Phone number cannot be empty")
        }
        return authRepository.login(phone)
    }
}
