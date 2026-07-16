package com.example.rencar_pair.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RentalPhotoResponse(
    val side: String,
    val imageUrl: String,
    val createdAt: String
)

@Serializable
data class RentalPhotosStateResponse(
    val rentalId: String,
    val photos: List<RentalPhotoResponse> = emptyList(),
    val uploadedCount: Int = photos.size,
    val remainingSides: List<String> = emptyList(),
    val photosComplete: Boolean = false
)
