package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.safeApiCall
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
        return safeApiCall(
            call = { api.getLicenseStatus() },
            transform = { it.toDomain() }
        ).let { result ->
            when (result) {
                is NetworkResult.Success -> result
                is NetworkResult.Error -> NetworkResult.Success(
                    DriverLicense(LicenseStatus.NotUploaded)
                )
            }
        }
    }

    override suspend fun upload(frontPath: String, backPath: String): NetworkResult<DriverLicense> {
        return safeApiCall(
            call = {
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
                api.uploadLicense(frontPart, backPart)
            },
            transform = { it.toDomain() }
        )
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
