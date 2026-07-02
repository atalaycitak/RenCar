package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.AuthUserResponse
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.model.UserRole
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
                if (response.code() == 401) {
                    return NetworkResult.Error("Register olmadan login olamazsiniz.", response.code())
                }
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
                response.body()?.let { saveAuthResponse(it) } ?: NetworkResult.Error("Empty response body")
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
                response.body()?.let { saveAuthResponse(it) } ?: NetworkResult.Error("Empty response body")
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

    override suspend fun refreshSession(): NetworkResult<User> {
        val refreshToken = dataStore.getRefreshToken()
            ?: return NetworkResult.Error("Refresh token missing")

        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let { saveAuthResponse(it) } ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Session refresh failed",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getCurrentUser(): NetworkResult<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val token = dataStore.authToken.firstOrNull().orEmpty()
                response.body()?.let { NetworkResult.Success(it.toDomain(token)) }
                    ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Current user fetch failed",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun logout(): NetworkResult<String> {
        return try {
            val response = api.logout()
            dataStore.clear()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.message ?: "Cikis yapildi")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Logout failed",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            dataStore.clear()
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getSavedToken(): String? {
        return dataStore.authToken.firstOrNull()
    }

    override suspend fun clearSession() {
        dataStore.clear()
    }

    private suspend fun saveAuthResponse(body: AuthResponse): NetworkResult<User> {
        val token = body.accessToken.orEmpty()
        if (token.isBlank()) {
            return NetworkResult.Error("Auth token missing")
        }
        dataStore.saveAuthToken(token)
        body.refreshToken?.let { dataStore.saveRefreshToken(it) }
        val user = body.user?.toDomain(token) ?: User(id = "", fullName = "", token = token)
        if (user.id.isNotBlank()) {
            dataStore.saveUserId(user.id)
        }
        return NetworkResult.Success(user)
    }

    private fun AuthUserResponse.toDomain(token: String): User {
        return User(
            id = id,
            fullName = fullName,
            token = token,
            role = when (role?.uppercase()) {
                "CUSTOMER" -> UserRole.Customer
                "ADMIN" -> UserRole.Admin
                else -> UserRole.Pending
            }
        )
    }
}
