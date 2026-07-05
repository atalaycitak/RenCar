package com.example.rencar_pair.presentation.ui.screens.return_vehicle

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.ReturnAngle
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class ReturnVehicleState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val frontPhotoUri: String? = null,
    val backPhotoUri: String? = null,
    val leftPhotoUri: String? = null,
    val rightPhotoUri: String? = null
) : MviState {

    val allPhotosFilled: Boolean
        get() = frontPhotoUri != null && backPhotoUri != null &&
                leftPhotoUri != null && rightPhotoUri != null
}

sealed interface ReturnVehicleIntent : MviIntent {
    data class AddPhoto(val angle: ReturnAngle, val uri: String) : ReturnVehicleIntent
    data class SubmitReturn(val rentalId: String) : ReturnVehicleIntent
}

sealed interface ReturnVehicleEffect : MviEffect {
    data object NavigateToHome : ReturnVehicleEffect
    data class ShowError(val message: String) : ReturnVehicleEffect
}
