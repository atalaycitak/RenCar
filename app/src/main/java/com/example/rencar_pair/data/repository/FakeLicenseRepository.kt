package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.repository.LicenseRepository
import kotlinx.coroutines.delay

class FakeLicenseRepository : LicenseRepository {
    private var currentLicense = DriverLicense(
        status = LicenseStatus.NotUploaded
    )

    override suspend fun getStatus(): NetworkResult<DriverLicense> {
        delay(800)
        return NetworkResult.Success(currentLicense)
    }

    override suspend fun upload(frontPath: String, backPath: String, selfiePath: String): NetworkResult<DriverLicense> {
        delay(1500)
        currentLicense = currentLicense.copy(
            status = LicenseStatus.Pending,
            frontImageUrl = frontPath,
            backImageUrl = backPath
        )
        return NetworkResult.Success(currentLicense)
    }
}
