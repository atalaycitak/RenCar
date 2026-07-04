package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AdminLicenseResponse
import com.example.rencar_pair.data.remote.dto.AdminRentalResponse
import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.CreateVehicleRequest
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.LicenseUploadResponse
import com.example.rencar_pair.data.remote.dto.MessageResponse
import com.example.rencar_pair.data.remote.dto.OtpRequiredResponseDto
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import com.example.rencar_pair.data.remote.dto.RejectLicenseRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.dto.AuthUserResponse
import com.example.rencar_pair.data.remote.dto.UpdateVehicleRequest
import com.example.rencar_pair.data.remote.dto.VehiclePositionResponse
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.POST

interface RenCarApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<OtpRequiredResponseDto>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<AuthUserResponse>

    @Multipart
    @POST("license/upload")
    suspend fun uploadLicense(
        @Part front: MultipartBody.Part,
        @Part back: MultipartBody.Part
    ): Response<LicenseUploadResponse>

    @GET("license/status")
    suspend fun getLicenseStatus(): Response<LicenseStatusResponse>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleResponse>>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleResponse>

    @POST("rentals")
    suspend fun createRental(@Body request: CreateRentalRequest): Response<RentalResponse>

    @GET("rentals")
    suspend fun getRentals(): Response<List<RentalResponse>>

    @GET("rentals/{id}")
    suspend fun getRental(@Path("id") id: String): Response<RentalResponse>

    @POST("rentals/{id}/return")
    suspend fun returnRental(@Path("id") id: String): Response<RentalResponse>

    @GET("health")
    suspend fun health(): Response<Unit>

    @GET("customer/ping")
    suspend fun customerPing(): Response<Unit>

    @GET("admin/ping")
    suspend fun adminPing(): Response<Unit>

    @GET("admin/licenses")
    suspend fun getAdminLicenses(): Response<List<AdminLicenseResponse>>

    @GET("admin/licenses/{id}")
    suspend fun getAdminLicense(@Path("id") id: String): Response<AdminLicenseResponse>

    @PATCH("admin/licenses/{id}/approve")
    suspend fun approveLicense(@Path("id") id: String): Response<AdminLicenseResponse>

    @PATCH("admin/licenses/{id}/reject")
    suspend fun rejectLicense(
        @Path("id") id: String,
        @Body request: RejectLicenseRequest
    ): Response<AdminLicenseResponse>

    @GET("admin/vehicles")
    suspend fun getAdminVehicles(): Response<List<VehicleResponse>>

    @POST("admin/vehicles")
    suspend fun createAdminVehicle(@Body request: CreateVehicleRequest): Response<VehicleResponse>

    @GET("admin/vehicles/{id}")
    suspend fun getAdminVehicle(@Path("id") id: String): Response<VehicleResponse>

    @PATCH("admin/vehicles/{id}")
    suspend fun updateAdminVehicle(
        @Path("id") id: String,
        @Body request: UpdateVehicleRequest
    ): Response<VehicleResponse>

    @DELETE("admin/vehicles/{id}")
    suspend fun deleteAdminVehicle(@Path("id") id: String): Response<Unit>

    @GET("admin/rentals")
    suspend fun getAdminRentals(): Response<List<AdminRentalResponse>>

    @GET("admin/rentals/{id}")
    suspend fun getAdminRental(@Path("id") id: String): Response<AdminRentalResponse>

    @GET("admin/locations")
    suspend fun getAdminLocations(): Response<List<VehiclePositionResponse>>
}
