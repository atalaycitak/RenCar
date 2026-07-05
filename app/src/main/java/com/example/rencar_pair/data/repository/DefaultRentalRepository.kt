package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.RentalRepository

class DefaultRentalRepository(
    private val api: RenCarApi
) : RentalRepository {

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
}
