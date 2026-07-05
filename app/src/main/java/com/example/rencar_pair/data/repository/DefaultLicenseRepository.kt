package com.example.rencar_pair.data.repository

import android.content.Context
import android.net.Uri
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.LicenseUploadResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.repository.LicenseRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DefaultLicenseRepository(
    private val api: RenCarApi,
    private val context: Context
) : LicenseRepository {

    override suspend fun getStatus(): NetworkResult<DriverLicense> {
        return safeApiCall(
            call = { api.getLicenseStatus() },
            transform = { it.toDomain() }
        )
    }

    override suspend fun upload(frontPath: String, backPath: String): NetworkResult<DriverLicense> {
        return safeApiCall(
            call = {
                val frontPart = MultipartBody.Part.createFormData(
                    "front",
                    resolveFileName(frontPath, "front-license.jpg"),
                    createImageRequestBody(frontPath)
                )
                val backPart = MultipartBody.Part.createFormData(
                    "back",
                    resolveFileName(backPath, "back-license.jpg"),
                    createImageRequestBody(backPath)
                )
                api.uploadLicense(frontPart, backPart)
            },
            transform = { it.toDomain() }
        )
    }

    private fun LicenseStatusResponse.toDomain(): DriverLicense {
        return DriverLicense(
            status = statusStringToEnum(status),
            frontImageUrl = frontImageUrl,
            backImageUrl = backImageUrl,
            rejectReason = rejectReason
        )
    }

    private fun LicenseUploadResponse.toDomain(): DriverLicense {
        return DriverLicense(
            status = statusStringToEnum(status),
            frontImageUrl = frontImageUrl,
            backImageUrl = backImageUrl,
            rejectReason = rejectReason
        )
    }

    private fun statusStringToEnum(status: String): LicenseStatus {
        return when (status.uppercase()) {
            "PENDING", "UNDER_REVIEW" -> LicenseStatus.Pending
            "APPROVED" -> LicenseStatus.Approved
            "REJECTED" -> LicenseStatus.Rejected
            "NOT_SUBMITTED" -> LicenseStatus.NotUploaded
            else -> LicenseStatus.NotUploaded
        }
    }

    private fun createImageRequestBody(source: String): RequestBody {
        return if (source.startsWith("content://")) {
            val uri = Uri.parse(source)
            val mediaType = (context.contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull()
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Image file cannot be read")
            bytes.toRequestBody(mediaType)
        } else {
            val file = File(source)
            if (!file.exists()) {
                throw IllegalArgumentException("Image file not found")
            }
            file.asRequestBody("image/*".toMediaTypeOrNull())
        }
    }

    private fun resolveFileName(source: String, fallback: String): String {
        return if (source.startsWith("content://")) {
            fallback
        } else {
            File(source).name.ifBlank { fallback }
        }
    }
}
