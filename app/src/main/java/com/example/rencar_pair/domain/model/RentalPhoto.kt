package com.example.rencar_pair.domain.model

enum class RentalPhotoSide {
    Front, Back, Left, Right;

    fun toApiValue(): String = when (this) {
        Front -> "FRONT"
        Back -> "BACK"
        Left -> "LEFT"
        Right -> "RIGHT"
    }

    companion object {
        fun fromApiString(value: String?): RentalPhotoSide? = when (value?.uppercase()) {
            "FRONT" -> Front
            "BACK" -> Back
            "LEFT" -> Left
            "RIGHT" -> Right
            else -> null
        }
    }
}

data class RentalPhotosState(
    val rentalId: String,
    val uploadedSides: Set<RentalPhotoSide>,
    val remainingSides: Set<RentalPhotoSide>,
    val photosComplete: Boolean
) {
    val uploadedCount: Int = uploadedSides.size
}
