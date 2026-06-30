package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.local.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { dataStoreManager.authToken.firstOrNull() }

        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (!token.isNullOrBlank() && !original.url.encodedPath.contains("/auth/")) {
            builder.header("Authorization", "Bearer $token")
        }

        val request = builder
            .method(original.method, original.body)
            .build()

        return chain.proceed(request)
    }
}
