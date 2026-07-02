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

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return try {
            val response = api.getVehicle(id)
            if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it.toDomain()) }
                    ?: fallbackVehicle(id)
            } else {
                fallbackVehicle(id)
            }
        } catch (e: Exception) {
            fallbackVehicle(id)
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
            longitude = longitude,
            rangeKm = estimateRange(type),
            locationName = "Istanbul"
        )
    }

    private fun fallbackVehicle(id: String): NetworkResult<Vehicle> {
        val vehicle = sampleVehicles.firstOrNull { it.id == id } ?: sampleVehicles.first()
        return NetworkResult.Success(vehicle)
    }

    private fun estimateRange(type: String): Int {
        return when (type.uppercase()) {
            "SUV", "MINIVAN" -> 420
            "SEDAN" -> 380
            else -> 320
        }
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
            longitude = 28.9784,
            rangeKm = 330,
            locationName = "Sultanahmet"
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
            longitude = 28.9799,
            rangeKm = 410,
            locationName = "Karakoy"
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
            longitude = 28.9654,
            rangeKm = 300,
            locationName = "Beyoglu"
        )
    )
}
