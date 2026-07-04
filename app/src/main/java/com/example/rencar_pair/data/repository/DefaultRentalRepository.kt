package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.RentalRepository
import kotlinx.coroutines.delay

class DefaultRentalRepository : RentalRepository {

    override suspend fun returnVehicle(rentalId: String, photos: List<String>): NetworkResult<Unit> {
        delay(2000)
        if (photos.size != 4) {
            return NetworkResult.Error("Dört açıdan da fotoğraf yüklenmelidir")
        }
        return NetworkResult.Success(Unit)
    }
}
