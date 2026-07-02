package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.User

interface AuthRepository {
    suspend fun login(phone: String): NetworkResult<String>
    suspend fun verifyOtp(phone: String, code: String): NetworkResult<User>
    suspend fun register(fullName: String, email: String, phone: String, password: String): NetworkResult<User>
    suspend fun getSavedToken(): String?
    suspend fun clearSession()
}
