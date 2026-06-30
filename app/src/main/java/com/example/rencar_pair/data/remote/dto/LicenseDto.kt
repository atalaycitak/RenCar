package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadLicenseRequest(
    val front: String,
    val back: String
)

@Serializable
data class LicenseStatusResponse(
    val status: String,
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val rejectReason: String? = null,
    val reviewedAt: String? = null
)
