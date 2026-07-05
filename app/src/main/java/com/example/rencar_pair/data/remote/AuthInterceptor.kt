package com.example.rencar_pair.data.remote

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenHolder: TokenHolder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = tokenHolder.token

        val builder = original.newBuilder()
            .header("Accept", "application/json")

        if (original.header("Content-Type") == null) {
            builder.header("Content-Type", "application/json")
        }

        if (!token.isNullOrBlank() && requiresAuth(original.url)) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }

    private fun requiresAuth(url: HttpUrl): Boolean {
        val segments = url.pathSegments
        val first = segments.firstOrNull() ?: return true
        if (first == "health") return false
        if (first == "auth") {
            val second = segments.getOrNull(1) ?: return true
            return second !in publicAuthEndpoints
        }
        return true
    }

    private companion object {
        val publicAuthEndpoints = setOf("login", "register", "verify-otp", "refresh")
    }
}
