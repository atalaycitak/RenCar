package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    // Lazy injection to avoid circular dependency (OkHttp -> Authenticator -> Retrofit -> OkHttp)
    private val api: RenCarApi by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if the refresh call itself gets a 401
        if (response.request.url.pathSegments.lastOrNull() == "refresh") {
            logoutLocally()
            return null
        }

        if (response.code == 401) {
            val refreshToken = runBlocking { dataStoreManager.getRefreshToken() }
            if (refreshToken.isNullOrBlank()) {
                logoutLocally()
                return null
            }

            // Synchronously attempt to refresh the token on OkHttp's background thread
            return try {
                val refreshResponse = runBlocking {
                    api.refreshToken(RefreshTokenRequest(refreshToken))
                }

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newTokens = refreshResponse.body()!!
                    val newAccessToken = newTokens.accessToken

                    if (!newAccessToken.isNullOrBlank()) {
                        // Update in-memory token
                        tokenHolder.token = newAccessToken
                        
                        // Persist new tokens asynchronously
                        scope.launch {
                            dataStoreManager.saveAuthToken(newAccessToken)
                            newTokens.refreshToken?.let {
                                dataStoreManager.saveRefreshToken(it)
                            }
                        }

                        // Retry the failed request with the new access token
                        return response.request.newBuilder()
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
            } catch (e: Exception) {
                logoutLocally()
                null
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
}
