package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}

