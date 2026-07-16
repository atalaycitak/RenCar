package com.example.rencar_pair.data.repository

import java.time.Instant
import java.time.format.DateTimeParseException
import android.util.Log

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.CreateReservationRequest
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.dto.ReservationResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.ReservationStatus
import com.example.rencar_pair.domain.repository.ReservationRepository

class DefaultReservationRepository(
    private val api: RenCarApi
) : ReservationRepository {

    override suspend fun createReservation(vehicleId: String): NetworkResult<Reservation> {
        return safeApiCall(
            call = { api.createReservation(CreateReservationRequest(vehicleId)) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getActiveReservation(): NetworkResult<Reservation?> {
        return when (val result = safeApiCall(
            call = { api.getActiveReservation() },
            transform = { it.toDomain() }
        )) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Error -> if (result.code == 404) {
                NetworkResult.Success(null)
            } else {
                result
            }
        }
    }

    override suspend fun cancelReservation(id: String): NetworkResult<Unit> {
        return try {
            val response = api.cancelReservation(id)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Rezervasyon iptal edilemedi", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error("Rezervasyon iptal edilemedi: ${e.localizedMessage}")
        }
    }

    override suspend fun createRental(
        vehicleId: String,
        endDate: String?,
        plan: String?
    ): NetworkResult<Rental> {
        return safeApiCall(
            call = { api.createRental(CreateRentalRequest(vehicleId = vehicleId, plan = plan, endDate = endDate)) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return safeApiCall(
            call = { api.getRentals() },
            transform = { list -> list.orEmpty().mapNotNull { it.toDomainOrNull() } }
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
        val startIso = startedAt ?: startDate ?: createdAt
        val endIso = endDate ?: endedAt ?: startIso
        return Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            startDate = Instant.parse(startIso),
            endDate = Instant.parse(endIso),
            totalPrice = totalPrice ?: 0.0,
            status = RentalStatus.fromApiString(status)
        )
    }

    private fun ReservationResponse.toDomain(): Reservation {
        return Reservation(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            status = ReservationStatus.fromApiString(status),
            expiresAt = Instant.parse(expiresAt),
            remainingSeconds = remainingSeconds.coerceAtLeast(0),
            createdAt = Instant.parse(createdAt)
        )
    }

    private fun RentalResponse.toDomainOrNull(): Rental? {
        return try {
            toDomain()
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "Skipping rental $id: invalid date format", e)
            null
        }
    }

    private companion object {
        private const val TAG = "ReservationRepo"
    }
}
