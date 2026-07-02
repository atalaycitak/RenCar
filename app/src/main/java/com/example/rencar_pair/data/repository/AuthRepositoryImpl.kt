package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val api: RenCarApi,
    private val dataStore: DataStoreManager
) : AuthRepository {

    override suspend fun login(phone: String): NetworkResult<String> {
        return try {
            val response = api.login(LoginRequest(phone))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body.message)
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

    override suspend fun verifyOtp(phone: String, code: String): NetworkResult<User> {
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(phone, code))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val token = body.accessToken.orEmpty()
                    if (token.isBlank()) {
                        return NetworkResult.Error("Auth token missing")
                    }
                    val userId = body.user?.id.orEmpty()
                    val fullName = body.user?.fullName.orEmpty()
                    dataStore.saveAuthToken(token)
                    body.refreshToken?.let { dataStore.saveRefreshToken(it) }
                    dataStore.saveUserId(userId)
                    NetworkResult.Success(
                        User(
                            id = userId,
                            fullName = fullName,
                            token = token
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

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): NetworkResult<User> {
        return try {
            val response = api.register(RegisterRequest(fullName, email, phone, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val token = body.accessToken.orEmpty()
                    if (token.isBlank()) {
                        return NetworkResult.Error("Auth token missing")
                    }
                    val userId = body.user?.id.orEmpty()
                    val resolvedFullName = body.user?.fullName.orEmpty()
                    dataStore.saveAuthToken(token)
                    body.refreshToken?.let { dataStore.saveRefreshToken(it) }
                    dataStore.saveUserId(userId)
                    NetworkResult.Success(
                        User(
                            id = userId,
                            fullName = resolvedFullName,
                            token = token
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

    override suspend fun getSavedToken(): String? {
        return dataStore.authToken.firstOrNull()
    }

    override suspend fun clearSession() {
        dataStore.clear()
    }
}
