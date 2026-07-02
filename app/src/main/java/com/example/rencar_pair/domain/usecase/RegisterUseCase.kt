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
            return NetworkResult.Error("Ad soyad boş olamaz")
        }
        if (email.isBlank() || !email.contains("@")) {
            return NetworkResult.Error("Geçerli bir e-posta girin")
        }
        if (phone.isBlank()) {
            return NetworkResult.Error("Telefon numarası boş olamaz")
        }
        if (password.length < 6) {
            return NetworkResult.Error("Şifre en az 6 karakter olmalı")
        }
        return authRepository.register(fullName, email, phone, password)
    }
}
