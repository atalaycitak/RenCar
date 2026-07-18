package com.example.rencar_pair.presentation.ui.screens.home

import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.ReservationStatus
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

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
    fun `highlighted vehicle prefers nearest actionable vehicle over busy nearest vehicle`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(
                    id = "busy-near",
                    latitude = 41.0090,
                    longitude = 28.9790,
                    status = VehicleStatus.Rented,
                    canReserve = false
                ),
                mapInsightVehicle(id = "available-far", latitude = 41.0170, longitude = 28.9860)
            ),
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals("available-far", state.highlightedVehicle?.id)
    }

    @Test
    fun `highlighted vehicle keeps available map vehicle when active rental exists`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(id = "available-near", latitude = 41.0090, longitude = 28.9790),
                mapInsightVehicle(
                    id = "active-rental",
                    latitude = 41.0500,
                    longitude = 29.0200,
                    status = VehicleStatus.Rented,
                    canReserve = false
                )
            ),
            activeRental = ActiveRental(
                id = "rental-1",
                userId = "user-1",
                vehicleId = "active-rental",
                plan = RentalPlan.PerMinute,
                status = RentalStatus.Active,
                elapsedSeconds = 120.0,
                currentCost = 42.0,
                startedAt = Instant.parse("2026-07-18T10:00:00Z"),
                distanceKm = 1.2,
                durationMinutes = 2.0,
                startFee = 15.0,
                createdAt = Instant.parse("2026-07-18T10:00:00Z")
            ),
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals("available-near", state.highlightedVehicle?.id)
    }

    @Test
    fun `service area outside location does not hide fleet or show absurd eta`() {
        val vehicle = mapInsightVehicle(id = "istanbul-car", latitude = 41.0090, longitude = 28.9790)
        val state = HomeState(
            vehicles = listOf(vehicle),
            userLocation = UserLocation(latitude = 37.4219, longitude = -122.0840)
        )

        assertEquals(listOf("istanbul-car"), state.visibleVehicles.map { it.id })
        assertEquals(null, state.distanceInfoFor(vehicle))
    }

    @Test
    fun `selected vehicle takes priority over nearest vehicle`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(
                    id = "selected",
                    latitude = 41.0170,
                    longitude = 28.9860
                ),
                mapInsightVehicle(id = "near", latitude = 41.0090, longitude = 28.9790)
            ),
            selectedVehicleId = "selected",
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals("selected", state.highlightedVehicle?.id)
    }

    @Test
    fun `comfort filter matches vehicle segment when api type is unknown`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(
                    id = "comfort-by-segment",
                    latitude = 41.0090,
                    longitude = 28.9790,
                    type = VehicleType.Unknown,
                    segment = "COMFORT"
                ),
                mapInsightVehicle(
                    id = "economy-by-segment",
                    latitude = 41.0100,
                    longitude = 28.9800,
                    type = VehicleType.Unknown,
                    segment = "ECONOMY"
                )
            ),
            selectedVehicleType = VehicleType.Sedan,
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals(listOf("comfort-by-segment"), state.visibleVehicles.map { it.id })
    }

    @Test
    fun `active reservation locks visible vehicles to reserved vehicle`() {
        val state = HomeState(
            vehicles = listOf(
                mapInsightVehicle(id = "selected", latitude = 41.0500, longitude = 29.0200),
                mapInsightVehicle(
                    id = "reserved",
                    latitude = 41.0090,
                    longitude = 28.9790,
                    status = VehicleStatus.Reserved,
                    canReserve = false,
                    canUnlock = true
                )
            ),
            selectedVehicleId = "selected",
            activeReservation = Reservation(
                id = "reservation-1",
                userId = "user-1",
                vehicleId = "reserved",
                status = ReservationStatus.Active,
                expiresAt = Instant.parse("2026-07-18T10:15:00Z"),
                remainingSeconds = 840,
                createdAt = Instant.parse("2026-07-18T10:00:00Z")
            ),
            userLocation = UserLocation(latitude = 41.0082, longitude = 28.9784)
        )

        assertEquals(listOf("reserved"), state.visibleVehicles.map { it.id })
        assertEquals("reserved", state.highlightedVehicle?.id)
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
    longitude: Double,
    status: VehicleStatus = VehicleStatus.Available,
    canReserve: Boolean = status == VehicleStatus.Available,
    canUnlock: Boolean = false,
    type: VehicleType = VehicleType.Sedan,
    segment: String? = null
): Vehicle {
    return Vehicle(
        id = id,
        plate = "34 MAP $id",
        brand = "Test",
        model = id,
        type = type,
        pricePerDay = 900.0,
        status = status,
        latitude = latitude,
        longitude = longitude,
        rangeKm = 420,
        segment = segment,
        canReserve = canReserve,
        canUnlock = canUnlock
    )
}
