package com.example.rencar_pair.domain.usecase.rental

import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.repository.RentalRepository

class RentalPhotoUseCases(
    private val repository: RentalRepository
) {
    suspend fun getPhotos(rentalId: String) = repository.getPreparationPhotos(rentalId)

    suspend fun uploadPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ) = repository.uploadPreparationPhoto(rentalId, side, photoUri)

    suspend fun startRental(rentalId: String) = repository.startRental(rentalId)
}
