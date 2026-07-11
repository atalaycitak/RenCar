package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.compose.runtime.Stable
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
    val isCompleted: Boolean = false
) : MviState {
    val completedPhotoCount: Int = listOf(frontPhotoTaken, backPhotoTaken, leftPhotoTaken, rightPhotoTaken).count { it }
    val canComplete: Boolean = completedPhotoCount == 4
}

sealed interface DeliveryChecklistIntent : MviIntent {
    data object TakeFrontPhoto : DeliveryChecklistIntent
    data object TakeBackPhoto : DeliveryChecklistIntent
    data object TakeLeftPhoto : DeliveryChecklistIntent
    data object TakeRightPhoto : DeliveryChecklistIntent
    data object CompleteChecklist : DeliveryChecklistIntent
}

sealed interface DeliveryChecklistEffect : MviEffect
