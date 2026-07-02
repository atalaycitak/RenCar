package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.ReservationRepository

class CreateRentalUseCase(
    private val repository: ReservationRepository
) {
    suspend operator fun invoke(vehicleId: String, endDate: String) =
        repository.createRental(vehicleId, endDate)
}
