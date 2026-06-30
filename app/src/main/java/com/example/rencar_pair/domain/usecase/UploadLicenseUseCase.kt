package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.LicenseRepository

class UploadLicenseUseCase(
    private val repository: LicenseRepository
) {
    suspend operator fun invoke(front: String, back: String) = repository.upload(front, back)
}
