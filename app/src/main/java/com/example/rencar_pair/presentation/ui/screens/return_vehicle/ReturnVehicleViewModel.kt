package com.example.rencar_pair.presentation.ui.screens.return_vehicle

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.ReturnAngle
import com.example.rencar_pair.domain.usecase.rental.ReturnVehicleUseCase
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class ReturnVehicleViewModel(
    private val returnVehicleUseCase: ReturnVehicleUseCase
) : BaseMviViewModel<ReturnVehicleState, ReturnVehicleIntent, ReturnVehicleEffect>(
    ReturnVehicleState()
) {

    override fun onIntent(intent: ReturnVehicleIntent) {
        when (intent) {
            is ReturnVehicleIntent.AddPhoto -> addPhoto(intent.angle, intent.uri)
            is ReturnVehicleIntent.UpdateDamageNote -> updateDamageNote(intent.note)
            ReturnVehicleIntent.RequestReturnConfirmation -> requestReturnConfirmation()
            ReturnVehicleIntent.DismissReturnConfirmation -> updateState {
                it.copy(showReturnConfirmation = false)
            }
            is ReturnVehicleIntent.SubmitReturn -> submitReturn(intent.rentalId)
        }
    }

    private fun addPhoto(angle: ReturnAngle, uri: String) {
        updateState { current ->
            when (angle) {
                ReturnAngle.FRONT -> current.copy(frontPhotoUri = uri, errorMessage = null)
                ReturnAngle.BACK -> current.copy(backPhotoUri = uri, errorMessage = null)
                ReturnAngle.LEFT -> current.copy(leftPhotoUri = uri, errorMessage = null)
                ReturnAngle.RIGHT -> current.copy(rightPhotoUri = uri, errorMessage = null)
            }
        }
    }

    private fun updateDamageNote(note: String) {
        updateState { it.copy(damageNote = note, errorMessage = null) }
    }

    private fun requestReturnConfirmation() {
        if (!currentState().allPhotosFilled) {
            updateState { it.copy(errorMessage = "Lutfen dort acidan da fotograf yukleyin") }
            return
        }

        updateState { it.copy(showReturnConfirmation = true, errorMessage = null) }
    }

    private fun submitReturn(rentalId: String) {
        val current = currentState()

        if (!current.allPhotosFilled) {
            updateState { it.copy(errorMessage = "Lutfen dort acidan da fotograf yukleyin") }
            return
        }

        val photos = listOfNotNull(
            current.frontPhotoUri,
            current.backPhotoUri,
            current.leftPhotoUri,
            current.rightPhotoUri
        )

        launchCoroutine {
            updateState {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    showReturnConfirmation = false
                )
            }
            when (val result = returnVehicleUseCase(rentalId, photos, current.damageNote.trim())) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isLoading = false) }
                    emitEffect(ReturnVehicleEffect.NavigateToSummary(rentalId))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isLoading = false, errorMessage = result.message) }
                    emitEffect(ReturnVehicleEffect.ShowError(result.message))
                }
            }
        }
    }
}
