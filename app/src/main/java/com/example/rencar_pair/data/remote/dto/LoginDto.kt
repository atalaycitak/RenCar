package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null,
    val fullName: String? = null,
    val user: AuthUserResponse? = null
)

@Serializable
data class AuthUserResponse(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val fullName: String,
    val role: String? = null
)
