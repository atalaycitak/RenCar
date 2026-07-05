package com.example.rencar_pair.data.repository

import java.time.Instant
import java.time.format.DateTimeParseException
import android.util.Log

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.repository.ReservationRepository

class FakeReservationRepository : ReservationRepository {

    private val rentals = mutableListOf<Rental>()

    override suspend fun createRental(vehicleId: String, endDate: String): NetworkResult<Rental> {
        val rental = Rental(
            id = "local-${System.currentTimeMillis()}",
            userId = "",
            vehicleId = vehicleId,
            startDate = Instant.now(),
            endDate = parseEndDate(endDate),
            totalPrice = 0.0,
            status = RentalStatus.Active
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
            val updated = rentals[index].copy(status = RentalStatus.Completed)
            rentals[index] = updated
            NetworkResult.Success(updated)
        } else {
            NetworkResult.Error("Rental not found")
        }
    }

    private fun parseEndDate(iso: String): Instant {
        return try {
            Instant.parse(iso)
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "Invalid endDate '$iso', defaulting to +24h", e)
            Instant.now().plusSeconds(86400)
        }
    }

    private companion object {
        private const val TAG = "FakeReservationRepo"
    }
}
