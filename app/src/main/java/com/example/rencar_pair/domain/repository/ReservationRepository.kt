package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.Reservation

interface ReservationRepository {
    suspend fun createReservation(vehicleId: String): NetworkResult<Reservation> {
        return NetworkResult.Error("Reservation endpoint is not implemented")
    }

    suspend fun getActiveReservation(): NetworkResult<Reservation?> {
        return NetworkResult.Success(null)
    }

    suspend fun cancelReservation(id: String): NetworkResult<Unit> {
        return NetworkResult.Error("Reservation cancel endpoint is not implemented")
    }

    suspend fun createRental(
        vehicleId: String,
        endDate: String? = null,
        plan: String? = null
    ): NetworkResult<Rental>

    suspend fun getRentals(): NetworkResult<List<Rental>>
    suspend fun getRental(id: String): NetworkResult<Rental>
    suspend fun returnRental(id: String): NetworkResult<Rental>
}
