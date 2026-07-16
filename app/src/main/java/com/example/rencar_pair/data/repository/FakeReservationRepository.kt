package com.example.rencar_pair.data.repository

import java.time.Instant
import java.time.format.DateTimeParseException
import android.util.Log

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.ReservationStatus
import com.example.rencar_pair.domain.repository.ReservationRepository

class FakeReservationRepository : ReservationRepository {

    private val rentals = mutableListOf<Rental>()
    private var activeReservation: Reservation? = null

    override suspend fun createReservation(vehicleId: String): NetworkResult<Reservation> {
        val now = Instant.now()
        val reservation = Reservation(
            id = "reservation-${System.currentTimeMillis()}",
            userId = "local-user",
            vehicleId = vehicleId,
            status = ReservationStatus.Active,
            expiresAt = now.plusSeconds(15 * 60),
            remainingSeconds = 15 * 60,
            createdAt = now
        )
        activeReservation = reservation
        return NetworkResult.Success(reservation)
    }

    override suspend fun getActiveReservation(): NetworkResult<Reservation?> {
        return NetworkResult.Success(activeReservation)
    }

    override suspend fun cancelReservation(id: String): NetworkResult<Unit> {
        if (activeReservation?.id == id) {
            activeReservation = null
        }
        return NetworkResult.Success(Unit)
    }

    override suspend fun createRental(
        vehicleId: String,
        endDate: String?,
        plan: String?
    ): NetworkResult<Rental> {
        val now = Instant.now()
        val rental = Rental(
            id = "local-${System.currentTimeMillis()}",
            userId = "",
            vehicleId = vehicleId,
            startDate = now,
            endDate = endDate?.let { parseEndDate(it) } ?: now,
            totalPrice = if (plan == "PER_MINUTE") 0.0 else 1500.0,
            status = if (plan == "PER_MINUTE") RentalStatus.Preparing else RentalStatus.Active
        )
        if (activeReservation?.vehicleId == vehicleId) {
            activeReservation = activeReservation?.copy(status = ReservationStatus.Converted)
        }
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
