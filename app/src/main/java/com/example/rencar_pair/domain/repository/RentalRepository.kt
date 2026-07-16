package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState

interface RentalRepository {
    suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> {
        return NetworkResult.Error("Rental photos endpoint is not implemented")
    }

    suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        return NetworkResult.Error("Rental photo upload endpoint is not implemented")
    }

    suspend fun startRental(rentalId: String): NetworkResult<Unit> {
        return NetworkResult.Error("Rental start endpoint is not implemented")
    }

    suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit>
}
