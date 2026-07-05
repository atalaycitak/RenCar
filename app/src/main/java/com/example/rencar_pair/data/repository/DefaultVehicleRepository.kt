package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleRepository

class DefaultVehicleRepository(
    private val api: RenCarApi
) : VehicleRepository {

    override suspend fun getAvailableVehicles(
        type: String?,
        page: Int?,
        limit: Int?
    ): NetworkResult<List<Vehicle>> {
        return safeApiCall(
            call = { api.getVehicles(type = type, page = page, limit = limit) },
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
            type = VehicleType.fromApiString(type),
            pricePerDay = pricePerDay,
            status = VehicleStatus.fromApiString(status),
            latitude = latitude,
            longitude = longitude
        )
    }
}

