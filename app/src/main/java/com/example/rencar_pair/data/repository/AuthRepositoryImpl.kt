package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: RenCarApi
) : AuthRepository {

    override suspend fun login(email: String, password: String): NetworkResult<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(
                        User(
                            id = body.userId,
                            fullName = body.fullName,
                            token = body.token
                        )
                    )
                } else {
                    NetworkResult.Error("Empty response body")
                }
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Unknown error",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}
