package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.UploadLicenseRequest
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RenCarApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/license/upload")
    suspend fun uploadLicense(@Body request: UploadLicenseRequest): Response<LicenseStatusResponse>

    @GET("api/license/status")
    suspend fun getLicenseStatus(): Response<LicenseStatusResponse>

    @GET("api/vehicles")
    suspend fun getVehicles(): Response<List<VehicleResponse>>
}
