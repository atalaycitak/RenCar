package com.example.rencar_pair.domain.usecase.rental

import com.example.rencar_pair.domain.repository.ReservationRepository

class FinishRentalUseCase(private val repository: ReservationRepository) {
    suspend operator fun invoke(id: String) = repository.returnRental(id)
}
