package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response DTO for POST /license/upload endpoint.
 * This is distinct from [LicenseStatusResponse] which is returned by GET /license/status.
 * The upload endpoint returns a full LicenseResponseDto including id, image URLs, and timestamps.
 */
@Serializable
data class LicenseUploadResponse(
    val id: String,
    val status: String,
    val frontImageUrl: String,
    val backImageUrl: String,
    /** Yüz doğrulama selfie'si URL'i. D5 öncesi kayıtlarda null. */
    val selfieImageUrl: String? = null,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)
