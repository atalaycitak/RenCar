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

        if (!token.isNullOrBlank() && !original.url.encodedPath.contains("/auth/")) {
            builder.header("Authorization", "Bearer $token")
        }

        val request = builder
            .method(original.method, original.body)
            .build()

        return chain.proceed(request)
    }
}
