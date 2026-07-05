package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.RentalRepository
import kotlinx.coroutines.delay

class FakeRentalRepository : RentalRepository {

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
}
