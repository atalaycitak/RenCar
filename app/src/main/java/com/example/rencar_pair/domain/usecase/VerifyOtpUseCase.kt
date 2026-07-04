package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository

class VerifyOtpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phone: String, code: String): NetworkResult<User> {
        if (phone.isBlank() || code.isBlank()) {
            return NetworkResult.Error("Telefon numarası ve OTP kodu boş olamaz")
        }
        if (code.length != 6) {
            return NetworkResult.Error("OTP kodu 6 haneli olmalı")
        }
        return authRepository.verifyOtp(phone, code)
    }
}
