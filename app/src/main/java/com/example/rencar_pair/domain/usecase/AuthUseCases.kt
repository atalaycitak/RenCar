package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.AuthRepository

class AuthUseCases(
    private val authRepository: AuthRepository
) {
    suspend fun getCurrentUser() = authRepository.getCurrentUser()
    suspend fun refreshSession() = authRepository.refreshSession()
    suspend fun logout() = authRepository.logout()
    suspend fun getSavedToken(): String? = authRepository.getSavedToken()
}
