package com.example.rencar_pair.presentation.ui.screens.license

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class LicenseVerificationViewModel(
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val uploadLicenseUseCase: UploadLicenseUseCase
) : BaseMviViewModel<LicenseVerificationState, LicenseVerificationIntent, LicenseVerificationEffect>(
    LicenseVerificationState()
) {

    init {
        onIntent(LicenseVerificationIntent.LoadStatus)
    }

    override fun onIntent(intent: LicenseVerificationIntent) {
        when (intent) {
            LicenseVerificationIntent.LoadStatus -> loadStatus()
            LicenseVerificationIntent.PickFrontImage -> updateState {
                it.copy(hasFrontImage = true, errorMessage = null)
            }
            LicenseVerificationIntent.PickBackImage -> updateState {
                it.copy(hasBackImage = true, errorMessage = null)
            }
            LicenseVerificationIntent.Upload -> upload()
        }
    }

    fun continueToMap() {
        emitEffect(LicenseVerificationEffect.ContinueToMap)
    }

    private fun loadStatus() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getLicenseStatusUseCase()) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun upload() {
        val current = currentState()
        if (!current.hasFrontImage || !current.hasBackImage) {
            updateState { it.copy(errorMessage = "Ehliyetin on ve arka yuzu gerekli.") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = uploadLicenseUseCase(
                frontPath = "mock-front-image", backPath = "mock-back-image"
            )) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun applyLicense(license: DriverLicense) {
        updateState {
            it.copy(
                status = license.status,
                isLoading = false,
                rejectReason = license.rejectReason,
                errorMessage = null
            )
        }
        if (license.status == LicenseStatus.Approved || license.status == LicenseStatus.Pending) {
            continueToMap()
        }
    }
}
