package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.repository.ReservationRepository

class ReservationRepositoryImpl(
    private val api: RenCarApi
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
                    ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Rental creation failed",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getRentals(): NetworkResult<List<Rental>> {
        return try {
            val response = api.getRentals()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body().orEmpty().map { it.toDomain() })
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch rentals",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        return try {
            val response = api.getRental(id)
            if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it.toDomain()) }
                    ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch rental",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun returnRental(id: String): NetworkResult<Rental> {
        return try {
            val response = api.returnRental(id)
            if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it.toDomain()) }
                    ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to return rental",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
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
