package com.example.rencar_pair.domain.usecase.rental

import com.example.rencar_pair.domain.repository.ReservationRepository

class GetMyRentalsUseCase(private val repository: ReservationRepository) {
    suspend operator fun invoke() = repository.getRentals()
}
