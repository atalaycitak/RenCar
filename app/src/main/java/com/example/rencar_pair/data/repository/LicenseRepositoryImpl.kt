package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.UploadLicenseRequest
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.repository.LicenseRepository

class LicenseRepositoryImpl(
    private val api: RenCarApi
) : LicenseRepository {

    override suspend fun getStatus(): NetworkResult<DriverLicense> {
        return try {
            val response = api.getLicenseStatus()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.toDomain() ?: DriverLicense(LicenseStatus.NotUploaded))
            } else {
                NetworkResult.Success(DriverLicense(LicenseStatus.NotUploaded))
            }
        } catch (e: Exception) {
            NetworkResult.Success(DriverLicense(LicenseStatus.NotUploaded))
        }
    }

    override suspend fun upload(front: String, back: String): NetworkResult<DriverLicense> {
        return try {
            val response = api.uploadLicense(UploadLicenseRequest(front = front, back = back))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.toDomain() ?: DriverLicense(LicenseStatus.Pending))
            } else {
                NetworkResult.Error(response.errorBody()?.string() ?: "License upload failed", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Success(DriverLicense(LicenseStatus.Pending))
        }
    }

    private fun LicenseStatusResponse.toDomain(): DriverLicense {
        return DriverLicense(
            status = when (status.uppercase()) {
                "PENDING" -> LicenseStatus.Pending
                "APPROVED" -> LicenseStatus.Approved
                "REJECTED" -> LicenseStatus.Rejected
                else -> LicenseStatus.NotUploaded
            },
            frontImageUrl = frontImageUrl,
            backImageUrl = backImageUrl,
            rejectReason = rejectReason
        )
    }
}
