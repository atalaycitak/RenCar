package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.TokenHolder
import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.AuthUserResponse
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.User
import com.example.rencar_pair.domain.model.UserRole
import com.example.rencar_pair.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull

class DefaultAuthRepository(
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
        return persistAndBuildUser(rawResult)
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
        return persistAndBuildUser(rawResult)
    }

    /**
     * Refreshes the session by calling the real POST /auth/refresh endpoint.
     * On success, the new token pair is persisted and the updated User is returned.
     * On failure (expired/revoked refresh token), the local session is cleared.
     */
    override suspend fun refreshSession(): NetworkResult<User> {
        val savedRefreshToken = dataStore.getRefreshToken()
        if (savedRefreshToken.isNullOrBlank()) {
            return NetworkResult.Error("No refresh token available — please log in again")
        }
        val rawResult = safeApiCall(
            call = { api.refreshToken(RefreshTokenRequest(savedRefreshToken)) }
        )
        return when (rawResult) {
            is NetworkResult.Success -> persistAndBuildUser(rawResult)
            is NetworkResult.Error -> {
                // Refresh token is invalid or revoked — force logout
                clearSession()
                NetworkResult.Error(rawResult.message ?: "Session expired — please log in again")
            }
        }
    }

    /**
     * Fetches the current user profile from GET /auth/me using the live access token.
     * Returns a real User with up-to-date role information.
     */
    override suspend fun getCurrentUser(): NetworkResult<User> {
        val savedToken = getSavedToken()
        if (savedToken.isNullOrBlank()) {
            return NetworkResult.Error("No active session")
        }
        return safeApiCall(
            call = { api.getMe() },
            transform = { it.toUser(savedToken) }
        )
    }

    override suspend fun logout(): NetworkResult<String> {
        // Best-effort server-side logout: invalidates all active refresh tokens.
        // We clear the local session regardless of API result so the user
        // is always logged out even if the network call fails.
        try { api.logout() } catch (_: Exception) { }
        dataStore.clear()
        tokenHolder.token = null
        return NetworkResult.Success("Logged out")
    }

    override suspend fun getSavedToken(): String? {
        return tokenHolder.token ?: dataStore.authToken.firstOrNull()
    }

    override suspend fun clearSession() {
        tokenHolder.token = null
        dataStore.clear()
        dataStore.notifyTokenExpired()
    }

    private suspend fun persistAndBuildUser(
        rawResult: NetworkResult<AuthResponse>
    ): NetworkResult<User> {
        return when (rawResult) {
            is NetworkResult.Success -> {
                val body = rawResult.data
                val token = body.accessToken.orEmpty()
                if (token.isBlank()) {
                    NetworkResult.Error("Auth token missing")
                } else {
                    tokenHolder.token = token
                    dataStore.saveAuthToken(token)
                    body.refreshToken?.let { dataStore.saveRefreshToken(it) }
                    val userId = body.user?.id.orEmpty()
                    dataStore.saveUserId(userId)
                    NetworkResult.Success(body.user?.toUser(token) ?: User(
                        id = userId,
                        fullName = "",
                        token = token
                    ))
                }
            }
            is NetworkResult.Error -> rawResult
        }
    }

    private fun AuthUserResponse.toUser(token: String): User {
        return User(
            id = id,
            fullName = fullName,
            token = token,
            role = UserRole.fromApiString(role)
        )
    }
}
