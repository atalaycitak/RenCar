package com.example.rencar_pair.presentation.ui.screens.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense
import com.example.rencar_pair.domain.model.LicenseStatus
import com.example.rencar_pair.domain.usecase.GetLicenseStatusUseCase
import com.example.rencar_pair.domain.usecase.UploadLicenseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LicenseVerificationViewModel(
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val uploadLicenseUseCase: UploadLicenseUseCase
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
            LicenseVerificationIntent.PickFrontImage -> _state.update {
                it.copy(hasFrontImage = true, errorMessage = null)
            }
            LicenseVerificationIntent.PickBackImage -> _state.update {
                it.copy(hasBackImage = true, errorMessage = null)
            }
            LicenseVerificationIntent.Upload -> upload()
        }
    }

    fun continueToMap() {
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

    private fun upload() {
        val current = state.value
        if (!current.hasFrontImage || !current.hasBackImage) {
            _state.update { it.copy(errorMessage = "Ehliyetin on ve arka yuzu gerekli.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = uploadLicenseUseCase(front = "mock-front-image", back = "mock-back-image")) {
                is NetworkResult.Success -> applyLicense(result.data)
                is NetworkResult.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    private fun applyLicense(license: DriverLicense) {
        _state.update {
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
