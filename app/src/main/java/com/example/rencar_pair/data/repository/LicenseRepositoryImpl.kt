package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.repository.LicenseRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LicenseRepositoryImpl(
    private val api: RenCarApi
) : LicenseRepository {

    override suspend fun getStatus(): NetworkResult<DriverLicense> {
        return try {
            val response = api.getLicenseStatus()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.toDomain() ?: DriverLicense(LicenseStatus.NotUploaded))
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "License status check failed",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun upload(frontPath: String, backPath: String): NetworkResult<DriverLicense> {
        return try {
            val front = File(frontPath)
            val back = File(backPath)
            val frontPart = MultipartBody.Part.createFormData(
                "front", 
                front.name, 
                front.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val backPart = MultipartBody.Part.createFormData(
                "back", 
                back.name, 
                back.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val response = api.uploadLicense(frontPart, backPart)
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.toDomain() ?: DriverLicense(LicenseStatus.Pending))
            } else {
                NetworkResult.Error(response.errorBody()?.string() ?: "License upload failed", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Upload failed")
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
