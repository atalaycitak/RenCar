package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<User>
}
