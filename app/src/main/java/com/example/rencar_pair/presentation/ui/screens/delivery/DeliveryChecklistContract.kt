package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class DeliveryChecklistState(
    val rentalId: String,
    val vehicleId: String,
    val frontPhotoTaken: Boolean = false,
    val backPhotoTaken: Boolean = false,
    val leftPhotoTaken: Boolean = false,
    val rightPhotoTaken: Boolean = false,
    val selectedPhotoUris: Map<RentalPhotoSide, String> = emptyMap(),
    val failedPhotoSides: Set<RentalPhotoSide> = emptySet(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadingSide: RentalPhotoSide? = null,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
) : MviState {
    val completedPhotoCount: Int = listOf(frontPhotoTaken, backPhotoTaken, leftPhotoTaken, rightPhotoTaken).count { it }
    val canComplete: Boolean = completedPhotoCount == 4
    val remainingPhotoCount: Int = 4 - completedPhotoCount

    fun isPhotoTaken(side: RentalPhotoSide): Boolean = when (side) {
        RentalPhotoSide.Front -> frontPhotoTaken
        RentalPhotoSide.Back -> backPhotoTaken
        RentalPhotoSide.Left -> leftPhotoTaken
        RentalPhotoSide.Right -> rightPhotoTaken
    }

    fun selectedPhotoUri(side: RentalPhotoSide): String? = selectedPhotoUris[side]

    fun hasUploadFailed(side: RentalPhotoSide): Boolean = side in failedPhotoSides
}

sealed interface DeliveryChecklistIntent : MviIntent {
    data object LoadPhotos : DeliveryChecklistIntent
    data class SelectPhoto(val side: RentalPhotoSide, val photoUri: String) : DeliveryChecklistIntent
    data object CompleteChecklist : DeliveryChecklistIntent
}

sealed interface DeliveryChecklistEffect : MviEffect {
    data object ChecklistCompleted : DeliveryChecklistEffect
    data class ShowError(val message: String) : DeliveryChecklistEffect
}
