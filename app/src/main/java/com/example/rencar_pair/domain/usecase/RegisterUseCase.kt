package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): NetworkResult<User> {
        if (fullName.isBlank()) {
            return NetworkResult.Error("Full name cannot be empty")
        }
        if (email.isBlank() || !email.contains("@")) {
            return NetworkResult.Error("Invalid email format")
        }
        if (phone.isBlank()) {
            return NetworkResult.Error("Phone number cannot be empty")
        }
        if (password.length < 6) {
            return NetworkResult.Error("Password must be at least 6 characters")
        }
        return authRepository.register(fullName, email, phone, password)
    }
}
