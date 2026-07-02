package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.DriverLicense

import java.io.File

interface LicenseRepository {
    suspend fun getStatus(): NetworkResult<DriverLicense>

    suspend fun upload(front: File, back: File): NetworkResult<DriverLicense>
}
