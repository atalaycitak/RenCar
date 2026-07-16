package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.ReservationRepository

class RentalUseCases(
    private val reservationRepository: ReservationRepository
) {
    suspend fun createReservation(vehicleId: String) =
        reservationRepository.createReservation(vehicleId)

    suspend fun getActiveReservation() =
        reservationRepository.getActiveReservation()

    suspend fun cancelReservation(id: String) =
        reservationRepository.cancelReservation(id)

    suspend fun createRental(vehicleId: String, endDate: String? = null, plan: String? = null) =
        reservationRepository.createRental(vehicleId, endDate, plan)

    suspend fun getRental(id: String) = reservationRepository.getRental(id)

    suspend fun returnRental(id: String) = reservationRepository.returnRental(id)

    suspend fun getMyRentals() = reservationRepository.getRentals()
}
