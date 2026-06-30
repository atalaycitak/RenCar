package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.data.remote.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense

interface LicenseRepository {
    suspend fun getStatus(): NetworkResult<DriverLicense>

    suspend fun upload(front: String, back: String): NetworkResult<DriverLicense>
}
