package com.example.rencar_pair.presentation.ui.screens.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.RefreshSessionUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LicenseVerificationViewModel(
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val uploadLicenseUseCase: UploadLicenseUseCase,
    private val refreshSessionUseCase: RefreshSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LicenseVerificationState())
    val state = _state.asStateFlow()

    private val _effect = Channel<LicenseVerificationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(LicenseVerificationIntent.LoadStatus)
    }

    fun onIntent(intent: LicenseVerificationIntent) {
        when (intent) {
            LicenseVerificationIntent.LoadStatus -> loadStatus()
            is LicenseVerificationIntent.PickFrontImage -> _state.update {
                it.copy(frontImageUri = intent.uri, errorMessage = null)
            }
            is LicenseVerificationIntent.PickBackImage -> _state.update {
                it.copy(backImageUri = intent.uri, errorMessage = null)
            }
            LicenseVerificationIntent.Upload -> upload()
            LicenseVerificationIntent.Continue -> refreshStatusAndContinue()
        }
    }

    private fun continueToMap() {
        viewModelScope.launch {
            _effect.send(LicenseVerificationEffect.ContinueToMap)
        }
    }

    private fun loadStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getLicenseStatusUseCase()) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
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
        val current = state.value
        if (!current.hasFrontImage || !current.hasBackImage) {
            _state.update { it.copy(errorMessage = "Ehliyetin ön ve arka yüzü gerekli.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = uploadLicenseUseCase(
                frontPath = current.frontImageUri.orEmpty(),
                backPath = current.backImageUri.orEmpty()
            )) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun applyLicense(
        license: DriverLicense,
        navigateWhenApproved: Boolean = false
    ) {
        _state.update {
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
