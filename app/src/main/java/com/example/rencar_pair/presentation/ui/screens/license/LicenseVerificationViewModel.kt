package com.example.rencar_pair.presentation.ui.screens.license

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.RefreshSessionUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class LicenseVerificationViewModel(
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val uploadLicenseUseCase: UploadLicenseUseCase,
    private val refreshSessionUseCase: RefreshSessionUseCase
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
            when (val result = getLicenseStatusUseCase()) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun refreshStatusAndContinue() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getLicenseStatusUseCase()) {
                is NetworkResult.Success -> applyLicense(result.data, navigateWhenApproved = true)
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun upload() {
        val current = currentState()
        if (!current.hasFrontImage || !current.hasBackImage) {
            updateState { it.copy(errorMessage = "Ehliyetin ön ve arka yüzü gerekli.") }
            return
        }

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = uploadLicenseUseCase(
                    frontPath = current.frontImageUri.orEmpty(),
                    backPath = current.backImageUri.orEmpty()
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
            viewModelScope.launch {
                when (val refreshResult = refreshSessionUseCase()) {
                    is NetworkResult.Success -> continueToMap()
                    is NetworkResult.Error -> _state.update {
                        it.copy(errorMessage = refreshResult.message)
                    }
                    NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
