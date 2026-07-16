package com.example.rencar_pair.data.repository

import android.content.Context
import android.net.Uri
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.RentalPhotosStateResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.repository.RentalRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultRentalRepository(
    private val api: RenCarApi,
    private val context: Context
) : RentalRepository {

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> {
        return safeApiCall(
            call = { api.getRentalPhotos(rentalId) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        val filePart = photoUri.toMultipartPart()
            ?: return NetworkResult.Error("Fotoğraf dosyası okunamadı")
        val sidePart = side.toApiValue().toRequestBody("text/plain".toMediaTypeOrNull())

        return safeApiCall(
            call = { api.uploadRentalPhoto(rentalId, sidePart, filePart) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> {
        return safeApiCall(
            call = { api.startRental(rentalId) },
            transform = { Unit }
        )
    }

    override suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit> {
        if (photos.size != 4) {
            return NetworkResult.Error("Dort acidan da fotograf yuklenmelidir")
        }

        return safeApiCall(
            call = { api.returnRental(rentalId) },
            transform = { Unit }
        )
    }

    private fun String.toMultipartPart(): MultipartBody.Part? {
        return runCatching {
            val uri = Uri.parse(this)
            val contentResolver = context.contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "rental-photo.jpg"
            MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
        }.getOrNull()
    }

    private fun RentalPhotosStateResponse.toDomain(): RentalPhotosState {
        val remaining = remainingSides.mapNotNull { RentalPhotoSide.fromApiString(it) }.toSet()
        val uploaded = photos.mapNotNull { RentalPhotoSide.fromApiString(it.side) }.toSet()
        return RentalPhotosState(
            rentalId = rentalId,
            uploadedSides = uploaded,
            remainingSides = remaining,
            photosComplete = photosComplete
        )
    }
}
