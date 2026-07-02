package com.example.rencar_pair.presentation.ui.screens.license

import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.presentation.mvi.MviEffect
import com.example.rencar_pair.presentation.mvi.MviIntent
import com.example.rencar_pair.presentation.mvi.MviState

data class LicenseVerificationState(
    val status: LicenseStatus = LicenseStatus.NotUploaded,
    val hasFrontImage: Boolean = false,
    val hasBackImage: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rejectReason: String? = null
) : MviState

sealed interface LicenseVerificationIntent : MviIntent {
    data object LoadStatus : LicenseVerificationIntent
    data object PickFrontImage : LicenseVerificationIntent
    data object PickBackImage : LicenseVerificationIntent
    data object Upload : LicenseVerificationIntent
}

sealed class LicenseVerificationEffect : MviEffect() {
    data object ContinueToMap : LicenseVerificationEffect()
}
