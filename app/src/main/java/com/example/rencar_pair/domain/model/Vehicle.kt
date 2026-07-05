package com.example.rencar_pair.domain.model

/** Araç tipi — API'den gelen değerlerin tür güvenli temsili. */
enum class VehicleType {
    Sedan, Suv, Hatchback, Station, Minivan, Unknown;

    companion object {
        fun fromApiString(value: String?): VehicleType = when (value?.uppercase()) {
            "SEDAN"    -> Sedan
            "SUV"      -> Suv
            "HATCHBACK"-> Hatchback
            "STATION"  -> Station
            "MINIVAN"  -> Minivan
            else       -> Unknown
        }
    }
}

/** Araç durumu — API'den gelen değerlerin tür güvenli temsili. */
enum class VehicleStatus {
    Available, Rented, Maintenance, Unknown;

    companion object {
        fun fromApiString(value: String?): VehicleStatus = when (value?.uppercase()) {
            "AVAILABLE"   -> Available
            "RENTED"      -> Rented
            "MAINTENANCE" -> Maintenance
            else          -> Unknown
        }
    }
}

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val pricePerDay: Double,
    val status: VehicleStatus,
    val latitude: Double,
    val longitude: Double,
    val rangeKm: Int = 320,
    val locationName: String = "Istanbul"
) {
    val title: String = "$brand $model"
}
