package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.repository.ReservationRepository

class ReservationRepositoryImpl(
    private val api: RenCarApi,
    private val fallbackRepository: ReservationRepository = FakeReservationRepository()
) : ReservationRepository {

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        return try {
            val response = api.createRental(
                CreateRentalRequest(
                    vehicleId = vehicleId,
                    endDate = endDate
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it.toDomain()) }
                    ?: fallbackRepository.createRental(vehicleId, endDate)
            } else {
                fallbackRepository.createRental(vehicleId, endDate)
            }
        } catch (e: Exception) {
            fallbackRepository.createRental(vehicleId, endDate)
        }
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return try {
            val response = api.getRentals()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body().orEmpty().map { it.toDomain() })
            } else {
                fallbackRepository.getRentals()
            }
        } catch (e: Exception) {
            fallbackRepository.getRentals()
        }
    }

    private fun RentalResponse.toDomain(): Rental {
        return Rental(
            id = id,
            vehicleId = vehicleId,
            startDate = startDate,
            endDate = endDate,
            totalPrice = totalPrice,
            status = status
        )
    }
}
