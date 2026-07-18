package com.example.rencar_pair.domain.usecase.rental

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.repository.RentalRepository

class ReturnVehicleUseCase(private val repository: RentalRepository) {
    suspend fun getActiveRental() = repository.getActiveRental()

    suspend operator fun invoke(
        rentalId: String,
        photos: List<String>,
        damageNote: String
    ): NetworkResult<Unit> {
        return when (val finishResult = repository.finishRental(rentalId)) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> {
                if (!finishResult.shouldTryReturnEndpoint()) {
                    return finishResult
                }
                when (val returnResult = repository.returnRental(rentalId)) {
                    is NetworkResult.Success -> NetworkResult.Success(Unit)
                    is NetworkResult.Error -> returnResult
                }
            }
        }
    }

    private fun NetworkResult.Error.shouldTryReturnEndpoint(): Boolean {
        val normalized = message.lowercase()
        val mentionsLegacyReturn =
            "daily" in normalized ||
            "iade" in normalized ||
            "return" in normalized
        return mentionsLegacyReturn && code in setOf(400, 409, 422)
    }
}
