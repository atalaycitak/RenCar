package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateMapInsightTest {

    @Test
    fun `highlighted vehicle falls back to nearest vehicle when none is selected`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(id = "far", latitude = 41.0500, longitude = 29.0200),
                mapInsightVehicle(id = "near", latitude = 41.0090, longitude = 28.9790)
            ),
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals("near", state.highlightedVehicle?.id)
    }

    @Test
    fun `selected vehicle takes priority over nearest vehicle`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(id = "selected", latitude = 41.0500, longitude = 29.0200),
                mapInsightVehicle(id = "near", latitude = 41.0090, longitude = 28.9790)
            ),
            selectedVehicleId = "selected",
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals("selected", state.highlightedVehicle?.id)
    }

    @Test
    fun `distance info reports walkable distance and eta`() {
        val vehicle = mapInsightVehicle(id = "near", latitude = 41.0090, longitude = 28.9790)
        val state = HomeState(
            vehicles = listOf(vehicle),
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        val info = state.distanceInfoFor(vehicle)

        assertEquals("102 m", info?.distanceLabel)
        assertTrue((info?.walkingMinutes ?: 0) >= 1)
    }
}

private fun mapInsightVehicle(
    id: String,
    latitude: Double,
    longitude: Double
): Vehicle {
    return Vehicle(
        id = id,
        plate = "34 MAP $id",
        brand = "Test",
        model = id,
        type = VehicleType.Sedan,
        pricePerDay = 900.0,
        status = VehicleStatus.Available,
        latitude = latitude,
        longitude = longitude,
        rangeKm = 420
    )
}
