package com.example.rencar_pair.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenHolder: TokenHolder
) : Interceptor {

    // Auth endpoints that do NOT require an Authorization header.
    // /auth/logout and /auth/me DO require it, so they are excluded from this list.
    private val noAuthPaths = setOf("login", "register", "verify-otp", "refresh")

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = tokenHolder.token

        val builder = original.newBuilder()
            .header("Accept", "application/json")

        if (original.header("Content-Type") == null) {
            builder.header("Content-Type", "application/json")
        }

        val lastSegment = original.url.pathSegments.lastOrNull()
        val isUnauthenticatedEndpoint = lastSegment in noAuthPaths
        if (!token.isNullOrBlank() && !isUnauthenticatedEndpoint) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
