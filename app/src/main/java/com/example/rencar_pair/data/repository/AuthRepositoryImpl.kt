package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.TokenHolder
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl(
    private val api: RenCarApi,
    private val dataStore: DataStoreManager,
    private val tokenHolder: TokenHolder
) : AuthRepository {

    override suspend fun login(phone: String): NetworkResult<String> {
        return safeApiCall(
            call = { api.login(LoginRequest(phone)) },
            transform = { it.message }
        )
    }

    override suspend fun verifyOtp(phone: String, code: String): NetworkResult<User> {
        val rawResult = safeApiCall(
            call = { api.verifyOtp(VerifyOtpRequest(phone, code)) }
        )

        if (rawResult is NetworkResult.Success) {
            val body = rawResult.data
            val token = body.accessToken.orEmpty()
            tokenHolder.token = token
            dataStore.saveAuthToken(token)
            body.refreshToken?.let { dataStore.saveRefreshToken(it) }
            val userId = body.user?.id.orEmpty()
            dataStore.saveUserId(userId)
        }

        return when (rawResult) {
            is NetworkResult.Success -> {
                val body = rawResult.data
                val token = body.accessToken.orEmpty()
                if (token.isBlank()) {
                    NetworkResult.Error("Auth token missing")
                } else {
                    NetworkResult.Success(
                        User(
                            id = body.user?.id.orEmpty(),
                            fullName = body.user?.fullName.orEmpty(),
                            token = token
                        )
                    )
                }
            }
            is NetworkResult.Error -> rawResult
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): NetworkResult<User> {
        val rawResult = safeApiCall(
            call = { api.register(RegisterRequest(fullName, email, phone, password)) }
        )

        if (rawResult is NetworkResult.Success) {
            val body = rawResult.data
            val token = body.accessToken.orEmpty()
            tokenHolder.token = token
            dataStore.saveAuthToken(token)
            body.refreshToken?.let { dataStore.saveRefreshToken(it) }
            val userId = body.user?.id.orEmpty()
            dataStore.saveUserId(userId)
        }

        return when (rawResult) {
            is NetworkResult.Success -> {
                val body = rawResult.data
                val token = body.accessToken.orEmpty()
                if (token.isBlank()) {
                    NetworkResult.Error("Auth token missing")
                } else {
                    NetworkResult.Success(
                        User(
                            id = body.user?.id.orEmpty(),
                            fullName = body.user?.fullName.orEmpty(),
                            token = token
                        )
                    )
                }
            }
            is NetworkResult.Error -> rawResult
        }
    }

    override suspend fun getSavedToken(): String? {
        return tokenHolder.token ?: dataStore.authToken.firstOrNull()
    }

    override suspend fun clearSession() {
        tokenHolder.token = null
        dataStore.clear()
    }
}
