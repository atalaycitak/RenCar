package com.example.rencar_pair.data.repository

import java.time.Instant

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.repository.ReservationRepository

class DefaultReservationRepository(
    private val api: RenCarApi
) : ReservationRepository {

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        return safeApiCall(
            call = { api.createRental(CreateRentalRequest(vehicleId, endDate)) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return safeApiCall(
            call = { api.getRentals() },
            transform = { list -> list.orEmpty().map { it.toDomain() } }
        )
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        return safeApiCall(
            call = { api.getRental(id) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        return safeApiCall(
            call = { api.returnRental(id) },
            transform = { it.toDomain() }
        )
    }

    private fun RentalResponse.toDomain(): Rental {
        return Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            startDate = Instant.parse(startDate),
            endDate = Instant.parse(endDate),
            totalPrice = totalPrice,
            status = RentalStatus.fromApiString(status)
        )
    }
}
