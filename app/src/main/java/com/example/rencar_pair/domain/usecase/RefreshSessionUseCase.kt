package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.AuthRepository

class RefreshSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.refreshSession()
}

