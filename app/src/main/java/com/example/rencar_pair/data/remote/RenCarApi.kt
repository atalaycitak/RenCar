package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RenCarApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
