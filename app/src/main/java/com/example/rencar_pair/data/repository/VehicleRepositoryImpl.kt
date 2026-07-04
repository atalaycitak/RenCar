package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.repository.VehicleRepository

class VehicleRepositoryImpl(
    private val api: RenCarApi
) : VehicleRepository {

    override suspend fun getAvailableVehicles(): NetworkResult<List<Vehicle>> {
        return safeApiCall(
            call = { api.getVehicles() },
            transform = { list -> list.orEmpty().map { it.toDomain() } }
        )
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return safeApiCall(
            call = { api.getVehicle(id) },
            transform = { it.toDomain() }
        )
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
