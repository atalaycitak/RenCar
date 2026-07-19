package com.example.rencar_pair.domain.usecase.vehicle

import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.Reservation
import com.example.rencar_pair.domain.model.UserLocation
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleDistanceInfo
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class HomeVehicleFilterParams(
    val vehicles: List<Vehicle>,
    val userLocation: UserLocation?,
    val activeReservation: Reservation?,
    val activeRental: ActiveRental?,
    val pendingRental: Rental?,
    val selectedVehicleType: VehicleType?,
    val maxDailyPrice: Int?,
    val minRangeKm: Int?,
    val selectedVehicleId: String?
)

data class HomeVehicleData(
    val filteredVehicles: List<Vehicle>,
    val visibleVehicles: List<Vehicle>,
    val nearbyVehicles: List<Vehicle>,
    val actionableNearbyVehicles: List<Vehicle>,
    val selectedVehicle: Vehicle?,
    val activeReservationVehicle: Vehicle?,
    val activeRentalVehicle: Vehicle?,
    val pendingRentalVehicle: Vehicle?,
    val highlightedVehicle: Vehicle?,
    val distanceInfoMap: Map<String, VehicleDistanceInfo>
)

class FilterHomeVehiclesUseCase {

    fun invoke(params: HomeVehicleFilterParams): HomeVehicleData {
        val filteredVehicles = params.vehicles.filter { vehicle ->
            val matchesType = params.selectedVehicleType == null || vehicle.matchesCategory(params.selectedVehicleType)
            val matchesPrice = params.maxDailyPrice == null || vehicle.pricePerDay <= params.maxDailyPrice.toDouble()
            val matchesRange = params.minRangeKm == null || vehicle.rangeKm >= params.minRangeKm
            matchesType && matchesPrice && matchesRange
        }

        val activeReservationVehicle = params.activeReservation?.let { reservation ->
            reservation.vehicle ?: params.vehicles.firstOrNull { it.id == reservation.vehicleId }
        }

        val userLocation = params.userLocation
        val isUserInServiceArea = userLocation?.isInsideServiceArea() == true

        val visibleVehicles = activeReservationVehicle?.let { listOf(it) } ?: filteredVehicles
            .filter { it.isAvailableForMap() }
            .filter { vehicle ->
                if (userLocation != null && isUserInServiceArea) {
                    userLocation.walkingMinutesTo(vehicle) <= MAX_WALKING_MINUTES
                } else {
                    true
                }
            }

        val nearbyVehicles = if (userLocation != null && isUserInServiceArea) {
            visibleVehicles.sortedBy { vehicle -> userLocation.distanceKmTo(vehicle) }
        } else {
            visibleVehicles
        }

        val actionableNearbyVehicles = nearbyVehicles.filter { it.canReserve || it.canUnlock }

        val selectedVehicle = visibleVehicles.firstOrNull { it.id == params.selectedVehicleId }

        val activeRentalVehicle = params.activeRental?.vehicleId?.let { vehicleId ->
            filteredVehicles.firstOrNull { it.id == vehicleId }
        }

        val pendingRentalVehicle = params.pendingRental?.vehicleId?.let { vehicleId ->
            filteredVehicles.firstOrNull { it.id == vehicleId }
        }

        val highlightedVehicle = activeReservationVehicle
            ?: selectedVehicle
            ?: pendingRentalVehicle
            ?: actionableNearbyVehicles.firstOrNull()
            ?: nearbyVehicles.firstOrNull()

        val distanceInfoMap = buildMap {
            if (userLocation != null && isUserInServiceArea) {
                params.vehicles.forEach { vehicle ->
                    val distanceKm = userLocation.distanceKmTo(vehicle)
                    put(vehicle.id, VehicleDistanceInfo(distanceKm, distanceKm.toWalkingMinutes()))
                }
            }
        }

        return HomeVehicleData(
            filteredVehicles = filteredVehicles,
            visibleVehicles = visibleVehicles,
            nearbyVehicles = nearbyVehicles,
            actionableNearbyVehicles = actionableNearbyVehicles,
            selectedVehicle = selectedVehicle,
            activeReservationVehicle = activeReservationVehicle,
            activeRentalVehicle = activeRentalVehicle,
            pendingRentalVehicle = pendingRentalVehicle,
            highlightedVehicle = highlightedVehicle,
            distanceInfoMap = distanceInfoMap
        )
    }

    private fun Vehicle.isAvailableForMap(): Boolean {
        return status == VehicleStatus.Available || canReserve || canUnlock
    }

    private fun Vehicle.matchesCategory(category: VehicleType): Boolean {
        val normalizedSegment = segment?.trim()?.uppercase()
        return when (category) {
            VehicleType.Hatchback -> type == VehicleType.Hatchback ||
                normalizedSegment in setOf("ECONOMY", "ECONOMIC", "EKONOMIK", "EKONOMİK")
            VehicleType.Sedan -> type == VehicleType.Sedan ||
                normalizedSegment in setOf("COMFORT", "KONFOR")
            VehicleType.Suv -> type == VehicleType.Suv || normalizedSegment == "SUV"
            VehicleType.Station -> type == VehicleType.Station
            VehicleType.Minivan -> type == VehicleType.Minivan
            VehicleType.Unknown -> type == VehicleType.Unknown
        }
    }

    private fun UserLocation.distanceKmTo(vehicle: Vehicle): Double {
        val latitudeDelta = Math.toRadians(vehicle.latitude - latitude)
        val longitudeDelta = Math.toRadians(vehicle.longitude - longitude)
        val originLatitude = Math.toRadians(latitude)
        val targetLatitude = Math.toRadians(vehicle.latitude)

        val haversine = sin(latitudeDelta / 2).pow(2.0) +
            cos(originLatitude) * cos(targetLatitude) * sin(longitudeDelta / 2).pow(2.0)
        val centralAngle = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
        return EARTH_RADIUS_KM * centralAngle
    }

    private fun UserLocation.walkingMinutesTo(vehicle: Vehicle): Int {
        return distanceKmTo(vehicle).toWalkingMinutes()
    }

    private fun Double.toWalkingMinutes(): Int {
        return ((this / WALKING_SPEED_KM_PER_HOUR) * MINUTES_PER_HOUR)
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun UserLocation.isInsideServiceArea(): Boolean {
        val serviceCenter = Vehicle(
            id = "service-area-center",
            plate = "",
            brand = "",
            model = "",
            type = VehicleType.Unknown,
            pricePerDay = 0.0,
            status = VehicleStatus.Available,
            latitude = SERVICE_CENTER_LATITUDE,
            longitude = SERVICE_CENTER_LONGITUDE
        )
        return distanceKmTo(serviceCenter) <= SERVICE_AREA_RADIUS_KM
    }

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val WALKING_SPEED_KM_PER_HOUR = 4.8
        private const val MINUTES_PER_HOUR = 60
        private const val MAX_WALKING_MINUTES = 15
        private const val SERVICE_CENTER_LATITUDE = 41.0082
        private const val SERVICE_CENTER_LONGITUDE = 28.9784
        private const val SERVICE_AREA_RADIUS_KM = 120.0
    }
}
