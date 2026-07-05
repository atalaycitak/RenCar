package com.example.rencar_pair.domain.model

enum class LicenseStatus {
    NotUploaded,
    Pending,
    Approved,
    Rejected
}

data class DriverLicense(
    val status: LicenseStatus,
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val rejectReason: String? = null
)
