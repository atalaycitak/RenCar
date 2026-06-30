package com.example.rencar_pair.data.repository

import com.example.rencar_pair.data.remote.NetworkResult
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
                NetworkResult.Success(vehicles.ifEmpty { sampleVehicles })
            } else {
                NetworkResult.Success(sampleVehicles)
            }
        } catch (e: Exception) {
            NetworkResult.Success(sampleVehicles)
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

    private val sampleVehicles = listOf(
        Vehicle(
            id = "sample-1",
            plate = "34 RNC 001",
            brand = "Renault",
            model = "Clio",
            type = "HATCHBACK",
            pricePerDay = 450.0,
            status = "AVAILABLE",
            latitude = 41.0082,
            longitude = 28.9784
        ),
        Vehicle(
            id = "sample-2",
            plate = "34 RNC 002",
            brand = "Fiat",
            model = "Egea",
            type = "SEDAN",
            pricePerDay = 520.0,
            status = "AVAILABLE",
            latitude = 41.0151,
            longitude = 28.9799
        ),
        Vehicle(
            id = "sample-3",
            plate = "34 RNC 003",
            brand = "Volkswagen",
            model = "Polo",
            type = "HATCHBACK",
            pricePerDay = 610.0,
            status = "AVAILABLE",
            latitude = 41.0049,
            longitude = 28.9654
        )
    )
}
