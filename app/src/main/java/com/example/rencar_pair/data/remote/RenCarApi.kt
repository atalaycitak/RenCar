package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.dto.UploadLicenseRequest
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("api/vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleResponse>

    @POST("api/rentals")
    suspend fun createRental(@Body request: CreateRentalRequest): Response<RentalResponse>

    @GET("api/rentals")
    suspend fun getRentals(): Response<List<RentalResponse>>
}
