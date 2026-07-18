package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val phone: String
)

@Serializable
data class OtpRequiredResponseDto(
    val message: String,
    val phone: String,
    val expiresAt: String
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val code: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    /** Davet kodu (opsiyonel, D6). Verilirse kayıt davet edene bağlanır. */
    val referralCode: String? = null
)

@Serializable
data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: AuthUserResponse? = null
)

@Serializable
data class AuthUserResponse(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val fullName: String,
    val role: String? = null,
    /** Davet kodu (D6). /auth/me çağrısında üretilir. */
    val referralCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
