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
        limit: Int?,
        includeBusy: Boolean
    ): NetworkResult<List<Vehicle>> {
        val vehicleTypeQuery = type?.takeIf { it in VEHICLE_TYPE_QUERIES }
        return safeApiCall(
            call = {
                api.getVehicles(
                    type = vehicleTypeQuery,
                    segment = type,
                    includeBusy = includeBusy.takeIf { it },
                    page = page,
                    limit = limit
                )
            },
            transform = { list ->
                list.orEmpty()
                    .map { it.toDomain() }
                    .filterNot { it.status == VehicleStatus.Maintenance }
            }
        )
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        return safeApiCall(
            call = { api.getVehicle(id) },
            transform = { it.toDomain() }
        )
    }

    private fun VehicleResponse.toDomain(): Vehicle {
        val vehicleStatus = VehicleStatus.fromApiString(status)
        return Vehicle(
            id = id,
            plate = plate,
            brand = brand,
            model = model,
            type = VehicleType.fromApiString(type),
            pricePerDay = pricePerDay,
            status = vehicleStatus,
            latitude = latitude,
            longitude = longitude,
            rangeKm = rangeKm ?: 320,
            locationName = locationName ?: "Istanbul",
            fuelLevelPercent = (fuelPercent ?: fuelLevelPercent)?.coerceIn(0, 100),
            transmission = transmission,
            seatCount = seats ?: seatCount,
            imageUrl = imageUrl,
            pricePerMinute = pricePerMinute,
            pricePerHour = pricePerHour,
            segment = segment,
            locationUpdatedAt = updatedAt,
            canReserve = canReserve ?: (vehicleStatus == VehicleStatus.Available),
            canUnlock = canUnlock ?: false
        )
    }

    private companion object {
        val VEHICLE_TYPE_QUERIES = setOf("SEDAN", "SUV", "HATCHBACK", "STATION", "MINIVAN")
    }
}

