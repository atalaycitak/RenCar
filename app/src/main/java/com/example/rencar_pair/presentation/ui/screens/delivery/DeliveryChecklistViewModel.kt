package com.example.rencar_pair.presentation.ui.screens.delivery

import androidx.lifecycle.SavedStateHandle
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.usecase.rental.RentalPhotoUseCases
import com.example.rencar_pair.presentation.mvi.BaseMviViewModel

class DeliveryChecklistViewModel(
    savedStateHandle: SavedStateHandle,
    private val rentalPhotoUseCases: RentalPhotoUseCases
) : BaseMviViewModel<DeliveryChecklistState, DeliveryChecklistIntent, DeliveryChecklistEffect>(
    DeliveryChecklistState(
        rentalId = savedStateHandle.get<String>("rentalId") ?: "",
        vehicleId = savedStateHandle.get<String>("vehicleId") ?: ""
    )
) {

    init {
        onIntent(DeliveryChecklistIntent.LoadPhotos)
    }

    override fun onIntent(intent: DeliveryChecklistIntent) {
        when (intent) {
            DeliveryChecklistIntent.LoadPhotos -> loadPhotos()
            is DeliveryChecklistIntent.SelectPhoto -> uploadPhoto(intent.side, intent.photoUri)
            DeliveryChecklistIntent.CompleteChecklist -> completeChecklist()
        }
    }

    private fun loadPhotos() {
        val rentalId = currentState().rentalId
        if (rentalId.isBlank()) return

        launchCoroutine {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            when (val result = rentalPhotoUseCases.getPhotos(rentalId)) {
                is NetworkResult.Success -> updateState {
                    it.withPhotos(result.data).copy(isLoading = false)
                }
                is NetworkResult.Error -> updateState {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun uploadPhoto(side: RentalPhotoSide, photoUri: String) {
        val rentalId = currentState().rentalId
        if (rentalId.isBlank()) return

        launchCoroutine {
            updateState {
                it.copy(
                    selectedPhotoUris = it.selectedPhotoUris + (side to photoUri),
                    failedPhotoSides = it.failedPhotoSides - side,
                    isUploading = true,
                    uploadingSide = side,
                    errorMessage = null
                )
            }
            when (val result = rentalPhotoUseCases.uploadPhoto(rentalId, side, photoUri)) {
                is NetworkResult.Success -> updateState {
                    it.withPhotos(result.data).copy(
                        isUploading = false,
                        uploadingSide = null,
                        failedPhotoSides = it.failedPhotoSides - side
                    )
                }
                is NetworkResult.Error -> {
                    val friendlyMessage = result.message.toPhotoUploadMessage()
                    updateState {
                        it.copy(
                            isUploading = false,
                            uploadingSide = null,
                            failedPhotoSides = it.failedPhotoSides + side,
                            errorMessage = friendlyMessage
                        )
                    }
                    emitEffect(DeliveryChecklistEffect.ShowError(friendlyMessage))
                }
            }
        }
    }

    private fun completeChecklist() {
        val current = currentState()
        if (!current.canComplete || current.isUploading) return

        launchCoroutine {
            updateState { it.copy(isUploading = true, errorMessage = null) }
            when (val result = rentalPhotoUseCases.startRental(current.rentalId)) {
                is NetworkResult.Success -> {
                    updateState { it.copy(isUploading = false, isCompleted = true) }
                    emitEffect(DeliveryChecklistEffect.ChecklistCompleted)
                }
                is NetworkResult.Error -> {
                    val friendlyMessage = result.message.toStartRentalMessage()
                    updateState { it.copy(isUploading = false, errorMessage = friendlyMessage) }
                    emitEffect(DeliveryChecklistEffect.ShowError(friendlyMessage))
                }
            }
        }
    }

    private fun DeliveryChecklistState.withPhotos(photosState: RentalPhotosState): DeliveryChecklistState {
        return copy(
            frontPhotoTaken = RentalPhotoSide.Front in photosState.uploadedSides,
            backPhotoTaken = RentalPhotoSide.Back in photosState.uploadedSides,
            leftPhotoTaken = RentalPhotoSide.Left in photosState.uploadedSides,
            rightPhotoTaken = RentalPhotoSide.Right in photosState.uploadedSides,
            errorMessage = null
        )
    }

    private fun String.toPhotoUploadMessage(): String {
        val normalized = lowercase()
        return when {
            "jpg" in normalized || "jpeg" in normalized || "png" in normalized ->
                "Bu fotoğraf yüklenemedi. Lütfen JPG veya PNG formatında bir görsel seç."
            "okun" in normalized || "read" in normalized ->
                "Fotoğraf okunamadı. Lütfen galeriden farklı bir JPG veya PNG görsel seç."
            else ->
                "Fotoğraf yüklenemedi. Lütfen farklı bir JPG veya PNG görsel ile tekrar dene."
        }
    }

    private fun String.toStartRentalMessage(): String {
        val normalized = lowercase()
        return when {
            "photo" in normalized || "foto" in normalized ->
                "Kiralamayı başlatmak için ön, arka, sol ve sağ fotoğrafların tamamını yüklemelisin."
            else ->
                "Kiralama başlatılamadı. Lütfen fotoğrafları kontrol edip tekrar dene."
        }
    }
}
