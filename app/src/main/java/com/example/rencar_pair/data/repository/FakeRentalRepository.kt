package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.repository.RentalRepository
import kotlinx.coroutines.delay

class FakeRentalRepository : RentalRepository {
    private val uploadedSidesByRental = mutableMapOf<String, Set<RentalPhotoSide>>()

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> {
        delay(250)
        return NetworkResult.Success(rentalId.toPhotoState())
    }

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        delay(400)
        val current = uploadedSidesByRental[rentalId].orEmpty()
        uploadedSidesByRental[rentalId] = current + side
        return NetworkResult.Success(rentalId.toPhotoState())
    }

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> {
        delay(500)
        val state = rentalId.toPhotoState()
        return if (state.photosComplete) {
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error("${state.remainingSides.size} fotoğraf kaldı")
        }
    }

    override suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit> {
        delay(800)
        if (photos.size != 4) {
            return NetworkResult.Error("Dort acidan da fotograf yuklenmelidir")
        }
        return NetworkResult.Success(Unit)
    }

    private fun String.toPhotoState(): RentalPhotosState {
        val uploaded = uploadedSidesByRental[this].orEmpty()
        val remaining = RentalPhotoSide.entries.toSet() - uploaded
        return RentalPhotosState(
            rentalId = this,
            uploadedSides = uploaded,
            remainingSides = remaining,
            photosComplete = remaining.isEmpty()
        )
    }
}
