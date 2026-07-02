package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.repository.VehicleRepository

class VehicleRepositoryImpl(
    private val api: RenCarApi
) : VehicleRepository {

    override suspend fun getAvailableVehicles(): NetworkResult<List<Vehicle>> {
        return try {
            val response = api.getVehicles()
            if (response.isSuccessful) {
                val vehicles = response.body().orEmpty().map { it.toDomain() }
                NetworkResult.Success(vehicles)
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch vehicles",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return try {
            val response = api.getVehicle(id)
            if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it.toDomain()) }
                    ?: NetworkResult.Error("Empty response body")
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch vehicle detail",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    private fun VehicleResponse.toDomain(): Vehicle {
        return Vehicle(
            id = id,
            plate = plate,
            brand = brand,
            model = model,
            type = type,
            pricePerDay = pricePerDay,
            status = status,
            latitude = latitude,
            longitude = longitude
        )
    }
}
