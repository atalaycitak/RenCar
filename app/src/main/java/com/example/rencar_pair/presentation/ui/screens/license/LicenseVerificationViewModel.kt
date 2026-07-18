package com.example.rencar_pair.presentation.ui.screens.license

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.usecase.AuthUseCases
import com.example.rencar_pair.domain.usecase.LicenseUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class LicenseVerificationViewModel(
    private val licenseUseCases: LicenseUseCases,
    private val authUseCases: AuthUseCases
) : BaseMviViewModel<LicenseVerificationState, LicenseVerificationIntent, LicenseVerificationEffect>(
    LicenseVerificationState()
) {

    init {
        onIntent(LicenseVerificationIntent.LoadStatus)
    }

    override fun onIntent(intent: LicenseVerificationIntent) {
        when (intent) {
            LicenseVerificationIntent.LoadStatus -> loadStatus()
            is LicenseVerificationIntent.PickFrontImage -> updateState {
                it.copy(
                    status = statusAfterNewImagePick(it.status),
                    frontImageUri = intent.uri,
                    errorMessage = null
                )
            }
            is LicenseVerificationIntent.PickBackImage -> updateState {
                it.copy(
                    status = statusAfterNewImagePick(it.status),
                    backImageUri = intent.uri,
                    errorMessage = null
                )
            }
            is LicenseVerificationIntent.PickSelfieImage -> updateState {
                it.copy(
                    status = statusAfterNewImagePick(it.status),
                    selfieImageUri = intent.uri,
                    errorMessage = null
                )
            }
            LicenseVerificationIntent.NextStep -> {
                val current = currentState()
                if (!current.hasFrontImage || !current.hasBackImage) {
                    updateState { s -> s.copy(errorMessage = "Lütfen ehliyetinizin ön ve arka yüzünü yükleyin.") }
                } else {
                    updateState { s -> s.copy(currentStep = 2, errorMessage = null) }
                }
            }
            LicenseVerificationIntent.PreviousStep -> {
                updateState { s -> s.copy(currentStep = 1, errorMessage = null) }
            }
            LicenseVerificationIntent.Upload -> upload()
            LicenseVerificationIntent.Continue -> refreshStatusAndContinue()
        }
    }

    fun continueToMap() {
        emitEffect(LicenseVerificationEffect.ContinueToMap)
    }

    private fun statusAfterNewImagePick(status: LicenseStatus): LicenseStatus {
        return when (status) {
            LicenseStatus.Rejected,
            LicenseStatus.Pending -> LicenseStatus.NotUploaded
            else -> status
        }
    }

    private fun loadStatus() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = licenseUseCases.getStatus()) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun refreshStatusAndContinue() {
        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = licenseUseCases.getStatus()) {
                is NetworkResult.Success -> applyLicense(result.data, navigateWhenApproved = true)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun upload() {
        val current = currentState()
        if (!current.hasFrontImage || !current.hasBackImage || !current.hasSelfieImage) {
            updateState { it.copy(errorMessage = "Ehliyetin ön, arka yüzü ve selfie gereklidir.") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = licenseUseCases.upload(
                    frontPath = current.frontImageUri.orEmpty(),
                    backPath = current.backImageUri.orEmpty(),
                    selfiePath = current.selfieImageUri.orEmpty()
                )
            ) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun applyLicense(
        license: DriverLicense,
        navigateWhenApproved: Boolean = false
    ) {
        updateState {
            it.copy(
                status = license.status,
                isLoading = false,
                rejectReason = license.rejectReason,
                errorMessage = if (navigateWhenApproved && license.status != LicenseStatus.Approved) {
                    "Ehliyet onayı tamamlanmadan devam edemezsiniz."
                } else {
                    null
                }
            )
        }
        if (navigateWhenApproved && license.status == LicenseStatus.Approved) {
            launchCoroutine {
                when (val refreshResult = authUseCases.refreshSession()) {
                    is NetworkResult.Success -> continueToMap()
                    is NetworkResult.Error -> updateState {
                        it.copy(errorMessage = refreshResult.message)
                    }
                }
            }
        }
    }
}
