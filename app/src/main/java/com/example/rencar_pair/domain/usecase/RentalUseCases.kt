package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.ReservationRepository

class RentalUseCases(
    private val reservationRepository: ReservationRepository
) {
    suspend fun createRental(vehicleId: String, endDate: String) =
        reservationRepository.createRental(vehicleId, endDate)

    suspend fun getActiveRental(id: String) = reservationRepository.getRental(id)

    suspend fun finishRental(id: String) = reservationRepository.returnRental(id)

    suspend fun getMyRentals() = reservationRepository.getRentals()
}
