package com.example.rencar_pair.domain.usecase.rental

import com.example.rencar_pair.domain.repository.RentalRepository

class ReturnVehicleUseCase(private val repository: RentalRepository) {
    suspend operator fun invoke(rentalId: String, photos: List<String>) =
        repository.returnVehicle(rentalId, photos)
}
