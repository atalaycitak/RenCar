package com.example.rencar_pair.data.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.VehicleRepository
import kotlinx.coroutines.delay

class FakeVehicleRepository : VehicleRepository {
    private val fakeVehicles = listOf(
        Vehicle(
            id = "v1",
            brand = "Renault",
            model = "Clio",
            plate = "34 ABC 123",
            type = VehicleType.Sedan,
            status = VehicleStatus.Available,
            pricePerDay = 1200.0,
            latitude = 41.0082,
            longitude = 28.9784,
            rangeKm = 420,
            locationName = "Sultanahmet, Istanbul"
        ),
        Vehicle(
            id = "v2",
            brand = "Tesla",
            model = "Model 3",
            plate = "34 TES 333",
            type = VehicleType.Sedan,
            status = VehicleStatus.Available,
            pricePerDay = 3500.0,
            latitude = 41.0390,
            longitude = 28.9835,
            rangeKm = 510,
            locationName = "Taksim, Istanbul"
        ),
        Vehicle(
            id = "v3",
            brand = "Dacia",
            model = "Duster",
            plate = "34 SUV 900",
            type = VehicleType.Suv,
            status = VehicleStatus.Available,
            pricePerDay = 2100.0,
            latitude = 41.0565,
            longitude = 29.0320,
            rangeKm = 380,
            locationName = "Besiktas, Istanbul"
        ),
        Vehicle(
            id = "v4",
            brand = "Fiat",
            model = "Egea",
            plate = "34 HCH 456",
            type = VehicleType.Hatchback,
            status = VehicleStatus.Available,
            pricePerDay = 950.0,
            latitude = 40.9909,
            longitude = 29.0302,
            rangeKm = 300,
            locationName = "Kadikoy, Istanbul"
        ),
        Vehicle(
            id = "v5",
            brand = "Mercedes",
            model = "Vito",
            plate = "34 VAN 777",
            type = VehicleType.Minivan,
            status = VehicleStatus.Available,
            pricePerDay = 2800.0,
            latitude = 41.0151,
            longitude = 28.9795,
            rangeKm = 460,
            locationName = "Karakoy, Istanbul"
        )
    )

    override suspend fun getAvailableVehicles(
        type: String?,
        page: Int?,
        limit: Int?
    ): NetworkResult<List<Vehicle>> {
        delay(800)
        val list = if (type != null) {
            fakeVehicles.filter { it.type.name.equals(type, ignoreCase = true) }
        } else {
            fakeVehicles
        }
        return NetworkResult.Success(list)
    }

    override suspend fun getVehicleDetail(id: String): NetworkResult<Vehicle> {
        delay(500)
        val vehicle = fakeVehicles.find { it.id == id }
        return if (vehicle != null) {
            NetworkResult.Success(vehicle)
        } else {
            NetworkResult.Error("Vehicle not found")
        }
    }
}
