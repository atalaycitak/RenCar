package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.repository.LicenseRepository

class LicenseUseCases(
    private val repository: LicenseRepository
) {
    suspend fun getStatus() = repository.getStatus()
    suspend fun upload(frontPath: String, backPath: String, selfiePath: String) = repository.upload(frontPath, backPath, selfiePath)
}
