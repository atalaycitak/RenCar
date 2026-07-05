package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult

interface RentalRepository {
    suspend fun returnVehicle(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit>
}
