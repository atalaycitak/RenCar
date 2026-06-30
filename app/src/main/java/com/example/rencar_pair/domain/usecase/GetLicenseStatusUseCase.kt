package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.LicenseRepository

class GetLicenseStatusUseCase(
    private val repository: LicenseRepository
) {
    suspend operator fun invoke() = repository.getStatus()
}
