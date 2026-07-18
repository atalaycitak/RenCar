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

    override suspend fun upload(frontPath: String, backPath: String, selfiePath: String): NetworkResult<DriverLicense> {
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
                val selfiePart = MultipartBody.Part.createFormData(
                    "selfie",
                    resolveFileName(selfiePath, "selfie.jpg"),
                    createImageRequestBody(selfiePath)
                )
                api.uploadLicense(frontPart, backPart, selfiePart)
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
            val type = context.contentResolver.getType(uri)
            // Eğer sistem "image/*" gibi belirsiz bir tip dönerse, varsayılan olarak "image/jpeg" kullan.
            val finalType = if (type == null || type.contains("*")) "image/jpeg" else type
            val mediaType = finalType.toMediaTypeOrNull()
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Image file cannot be read")
            bytes.toRequestBody(mediaType)
        } else {
            val path = if (source.startsWith("file://")) Uri.parse(source).path ?: source else source
            val file = File(path)
            if (!file.exists()) {
                throw IllegalArgumentException("Image file not found")
            }
            val ext = android.webkit.MimeTypeMap.getFileExtensionFromUrl(path)
            val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "image/jpeg"
            file.asRequestBody(mime.toMediaTypeOrNull())
        }
    }

    private fun resolveFileName(source: String, fallback: String): String {
        return if (source.startsWith("content://")) {
            fallback
        } else {
            val path = if (source.startsWith("file://")) Uri.parse(source).path ?: source else source
            File(path).name.ifBlank { fallback }
        }
    }
}
