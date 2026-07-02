package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenExpiredAuthenticator(
    private val dataStoreManager: DataStoreManager,
    private val authInterceptor: AuthInterceptor,
    private val json: Json
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401 && response.responseCount < MAX_AUTH_RETRIES) {
            authInterceptor.clearCachedToken()
            val newToken = runBlocking { refreshAccessToken() }
            if (!newToken.isNullOrBlank()) {
                authInterceptor.updateCachedToken(newToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
        }
        runBlocking { dataStoreManager.clear() }
        dataStoreManager.notifyTokenExpired()
        return null
    }

    private suspend fun refreshAccessToken(): String? {
        val refreshToken = dataStoreManager.getRefreshToken() ?: return null
        return try {
            val body = json.encodeToString(RefreshTokenRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://rencar.halitkalayci.com/auth/refresh")
                .post(body)
                .header("Accept", "application/json")
                .build()
            OkHttpClient.Builder().build().newCall(request).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) return null
                val auth = refreshResponse.body?.string()?.let {
                    json.decodeFromString<AuthResponse>(it)
                } ?: return null
                val accessToken = auth.accessToken.orEmpty()
                if (accessToken.isBlank()) return null
                dataStoreManager.saveAuthToken(accessToken)
                auth.refreshToken?.let { dataStoreManager.saveRefreshToken(it) }
                accessToken
            }
        } catch (e: Exception) {
            null
        }
    }

    private val Response.responseCount: Int
        get() {
            var current: Response? = this
            var count = 1
            while (current?.priorResponse != null) {
                count++
                current = current.priorResponse
            }
            return count
        }

    private companion object {
        const val MAX_AUTH_RETRIES = 2
    }
}
