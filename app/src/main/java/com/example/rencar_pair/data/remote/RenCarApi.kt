package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.OtpRequiredResponseDto
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST

interface RenCarApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<OtpRequiredResponseDto>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @Multipart
    @POST("api/license/upload")
    suspend fun uploadLicense(
        @Part front: MultipartBody.Part,
        @Part back: MultipartBody.Part
    ): Response<LicenseStatusResponse>

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

    @GET("api/rentals/{id}")
    suspend fun getRental(@Path("id") id: String): Response<RentalResponse>

    @POST("api/rentals/{id}/return")
    suspend fun returnRental(@Path("id") id: String): Response<RentalResponse>
}
