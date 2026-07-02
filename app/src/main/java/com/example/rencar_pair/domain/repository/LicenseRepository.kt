package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense

interface LicenseRepository {
    suspend fun getStatus(): NetworkResult<DriverLicense>

    suspend fun upload(frontPath: String, backPath: String): NetworkResult<DriverLicense>
}
