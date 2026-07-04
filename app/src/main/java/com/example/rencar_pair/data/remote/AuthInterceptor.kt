package com.example.rencar_pair.data.remote

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

        val isAuthEndpoint = original.url.pathSegments.contains("auth")
        if (!token.isNullOrBlank() && !isAuthEndpoint) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
