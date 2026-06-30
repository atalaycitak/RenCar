package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<User> {
        if (email.isBlank() || password.isBlank()) {
            return NetworkResult.Error("Email and password cannot be empty")
        }
        if (!email.contains("@")) {
            return NetworkResult.Error("Invalid email format")
        }
        if (password.length < 6) {
            return NetworkResult.Error("Password must be at least 6 characters")
        }
        return authRepository.login(email, password)
    }
}
