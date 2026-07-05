package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TokenExpiredAuthenticator(
    private val tokenHolder: TokenHolder,
    private val dataStoreManager: DataStoreManager
) : Authenticator, KoinComponent {

    private val api: RenCarApi by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.pathSegments.lastOrNull() == "refresh") {
            logoutLocally()
            return null
        }

        if (response.code == 401) {
            val requestThatFailed = response.request
            val oldToken = requestThatFailed.header("Authorization")?.removePrefix("Bearer ")

            return runBlocking {
                refreshMutex.withLock {
                    // Double-check: another request may have already refreshed the token
                    val currentToken = tokenHolder.token
                    if (!currentToken.isNullOrBlank() && currentToken != oldToken) {
                        return@withLock requestThatFailed.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                    }

                    try {
                        val refreshToken = withTimeout(REFRESH_TOKEN_TIMEOUT_MS) {
                            dataStoreManager.getRefreshToken()
                        }
                        if (refreshToken.isNullOrBlank()) {
                            logoutLocally()
                            return@withLock null
                        }

                        val refreshResponse = withTimeout(REFRESH_TOKEN_TIMEOUT_MS) {
                            api.refreshToken(RefreshTokenRequest(refreshToken))
                        }

                        if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                            val newTokens = refreshResponse.body()!!
                            val newAccessToken = newTokens.accessToken.orEmpty()

                            if (newAccessToken.isNotBlank()) {
                                tokenHolder.token = newAccessToken

                                scope.launch {
                                    dataStoreManager.saveAuthToken(newAccessToken)
                                    newTokens.refreshToken?.let {
                                        dataStoreManager.saveRefreshToken(it)
                                    }
                                }

                                return@withLock requestThatFailed.newBuilder()
                                    .header("Authorization", "Bearer $newAccessToken")
                                    .build()
                            } else {
                                logoutLocally()
                                null
                            }
                        } else {
                            logoutLocally()
                            null
                        }
                    } catch (_: Exception) {
                        logoutLocally()
                        null
                    }
                }
            }
        }
        return null
    }

    private fun logoutLocally() {
        tokenHolder.token = null
        scope.launch {
            dataStoreManager.clear()
            dataStoreManager.notifyTokenExpired()
        }
    }

    private companion object {
        const val REFRESH_TOKEN_TIMEOUT_MS = 10_000L
    }
}
