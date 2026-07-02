package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental

interface ReservationRepository {
    suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental>
    suspend fun getRentals(): NetworkResult<List<Rental>>
}
