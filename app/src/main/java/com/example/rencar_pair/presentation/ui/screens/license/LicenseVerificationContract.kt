package com.example.rencar_pair.presentation.ui.screens.license

import androidx.compose.runtime.Stable
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

@Stable
data class LicenseVerificationState(
    val status: LicenseStatus = LicenseStatus.NotUploaded,
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rejectReason: String? = null
) : MviState {
    val hasFrontImage: Boolean = frontImageUri != null
    val hasBackImage: Boolean = backImageUri != null
}

sealed interface LicenseVerificationIntent : MviIntent {
    data object LoadStatus : LicenseVerificationIntent
    data class PickFrontImage(val uri: String) : LicenseVerificationIntent
    data class PickBackImage(val uri: String) : LicenseVerificationIntent
    data object Upload : LicenseVerificationIntent
    data object Continue : LicenseVerificationIntent
}

sealed interface LicenseVerificationEffect : MviEffect {
    data object ContinueToMap : LicenseVerificationEffect
}
