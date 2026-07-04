package com.example.rencar_pair.domain.model

import androidx.compose.runtime.Immutable

enum class LicenseStatus {
    NotUploaded,
    Pending,
    Approved,
    Rejected
}

@Immutable
data class DriverLicense(
    val status: LicenseStatus,
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val rejectReason: String? = null
)
