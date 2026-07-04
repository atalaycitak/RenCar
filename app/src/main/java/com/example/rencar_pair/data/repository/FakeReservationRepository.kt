package com.example.rencar_pair.data.repository

import java.time.Instant

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.repository.ReservationRepository

class FakeReservationRepository : ReservationRepository {

    private val rentals = mutableListOf<Rental>()

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        val rental = Rental(
            id = "local-${System.currentTimeMillis()}",
            userId = "",
            vehicleId = vehicleId,
            startDate = Instant.now(),
            endDate = try { Instant.parse(endDate) } catch (e: Exception) { Instant.now().plusSeconds(86400) },
            totalPrice = 0.0,
            status = "ACTIVE"
        )
        rentals.add(0, rental)
        return NetworkResult.Success(rental)
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return NetworkResult.Success(rentals)
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        val rental = rentals.find { it.id == id }
        return if (rental != null) {
            NetworkResult.Success(rental)
        } else {
            NetworkResult.Error("Rental not found")
        }
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        val index = rentals.indexOfFirst { it.id == id }
        return if (index != -1) {
            val updated = rentals[index].copy(status = "RETURNED")
            rentals[index] = updated
            NetworkResult.Success(updated)
        } else {
            NetworkResult.Error("Rental not found")
        }
    }
}
