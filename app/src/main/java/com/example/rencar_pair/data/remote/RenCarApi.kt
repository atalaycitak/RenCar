package com.example.rencar_pair.data.remote

import com.example.rencar_pair.data.remote.dto.AdminLicenseResponse
import com.example.rencar_pair.data.remote.dto.AdminRentalResponse
import com.example.rencar_pair.data.remote.dto.AuthResponse
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.CreateVehicleRequest
import com.example.rencar_pair.data.remote.dto.EmptyResponse
import com.example.rencar_pair.data.remote.dto.LoginRequest
import com.example.rencar_pair.data.remote.dto.LicenseStatusResponse
import com.example.rencar_pair.data.remote.dto.LicenseUploadResponse
import com.example.rencar_pair.data.remote.dto.MessageResponse
import com.example.rencar_pair.data.remote.dto.OtpRequiredResponseDto
import com.example.rencar_pair.data.remote.dto.AddCardRequest
import com.example.rencar_pair.data.remote.dto.RefreshTokenRequest
import com.example.rencar_pair.data.remote.dto.RejectLicenseRequest
import com.example.rencar_pair.data.remote.dto.CardResponse
import com.example.rencar_pair.data.remote.dto.ProcessPaymentRequest
import com.example.rencar_pair.data.remote.dto.ProcessPaymentResponse
import com.example.rencar_pair.data.remote.dto.CreateReservationRequest
import com.example.rencar_pair.data.remote.dto.TopUpWalletRequest
import com.example.rencar_pair.data.remote.dto.VerifyOtpRequest
import com.example.rencar_pair.data.remote.dto.RegisterRequest
import com.example.rencar_pair.data.remote.dto.ReservationResponse
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.dto.RentalPhotosStateResponse
import com.example.rencar_pair.data.remote.dto.ActiveRentalResponse
import com.example.rencar_pair.data.remote.dto.FinishRentalResponse
import com.example.rencar_pair.data.remote.dto.QuoteResponse
import com.example.rencar_pair.data.remote.dto.RentalStatsResponse
import com.example.rencar_pair.data.remote.dto.AuthUserResponse
import com.example.rencar_pair.data.remote.dto.UpdateVehicleRequest
import com.example.rencar_pair.data.remote.dto.VehiclePositionResponse
import com.example.rencar_pair.data.remote.dto.VehicleResponse
import com.example.rencar_pair.data.remote.dto.WalletInfoResponse
import com.example.rencar_pair.data.remote.dto.CancelIyzicoPaymentRequest
import com.example.rencar_pair.data.remote.dto.CheckoutFormInitializeResponse
import com.example.rencar_pair.data.remote.dto.CreateIyzicoPaymentRequest
import com.example.rencar_pair.data.remote.dto.InitializeCheckoutFormRequest
import com.example.rencar_pair.data.remote.dto.IyzicoPaymentResponse
import com.example.rencar_pair.data.remote.dto.RefundIyzicoPaymentRequest
import com.example.rencar_pair.data.remote.dto.ThreedsInitializeResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import okhttp3.RequestBody
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

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
        @Part back: MultipartBody.Part,
        /** Yeni başvurularda zorunlu: yüz doğrulama selfie'si. */
        @Part selfie: MultipartBody.Part? = null
    ): Response<LicenseUploadResponse>

    @GET("license/status")
    suspend fun getLicenseStatus(): Response<LicenseStatusResponse>

    @GET("vehicles")
    suspend fun getVehicles(
        @Query("type") type: String? = null,
        @Query("segment") segment: String? = null,
        @Query("includeBusy") includeBusy: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<VehicleResponse>>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleResponse>

    /**
     * GET /vehicles/{id}/quote — Fiyat önizleme (salt hesap, araç kilitlenmez).
     * @param plan PER_MINUTE | HOURLY | DAILY
     * @param minutes Tahmini kullanım süresi (1..43200)
     */
    @GET("vehicles/{id}/quote")
    suspend fun getVehicleQuote(
        @Path("id") id: String,
        @Query("plan") plan: String,
        @Query("minutes") minutes: Int
    ): Response<QuoteResponse>

    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<ReservationResponse>

    @GET("reservations/active")
    suspend fun getActiveReservation(): Response<ReservationResponse>

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: String): Response<Unit>

    /** GET /rentals/stats — Aylık yolculuk özeti (sadece COMPLETED). */
    @GET("rentals/stats")
    suspend fun getRentalStats(
        @Query("month") month: String? = null
    ): Response<RentalStatsResponse>

    /** GET /rentals/active — Süren yolculuğun anlık durumu (elapsedSeconds, currentCost). */
    @GET("rentals/active")
    suspend fun getActiveRental(): Response<ActiveRentalResponse>

    @POST("rentals")
    suspend fun createRental(@Body request: CreateRentalRequest): Response<RentalResponse>

    @GET("rentals")
    suspend fun getRentals(): Response<List<RentalResponse>>

    @GET("rentals/{id}")
    suspend fun getRental(@Path("id") id: String): Response<RentalResponse>

    /**
     * POST /rentals/{id}/finish — Kullanım bazlı (PER_MINUTE/HOURLY) yolculuğu bitirir.
     * DAILY plan için POST /rentals/{id}/return kullanın.
     */
    @POST("rentals/{id}/finish")
    suspend fun finishRental(@Path("id") id: String): Response<FinishRentalResponse>

    /** POST /rentals/{id}/return — DAILY planına özel iade ucu (geriye uyum). */
    @POST("rentals/{id}/return")
    suspend fun returnRental(@Path("id") id: String): Response<RentalResponse>

    /** DELETE /rentals/{id} — PREPARING aşamasındaki yolculuğu iptal eder. */
    @DELETE("rentals/{id}")
    suspend fun cancelRental(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("rentals/{id}/photos")
    suspend fun uploadRentalPhoto(
        @Path("id") id: String,
        @Part("side") side: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<RentalPhotosStateResponse>

    @GET("rentals/{id}/photos")
    suspend fun getRentalPhotos(@Path("id") id: String): Response<RentalPhotosStateResponse>

    @POST("rentals/{id}/start")
    suspend fun startRental(@Path("id") id: String): Response<RentalResponse>

    @POST("rentals/{id}/pay")
    suspend fun processPayment(
        @Path("id") id: String,
        @Body request: ProcessPaymentRequest
    ): Response<ProcessPaymentResponse>

    @POST("cards")
    suspend fun addPaymentCard(@Body request: AddCardRequest): Response<CardResponse>

    @GET("cards")
    suspend fun getPaymentCards(): Response<List<CardResponse>>

    /** PATCH /cards/{id}/default — Kartı öntanımlı yapar. */
    @PATCH("cards/{id}/default")
    suspend fun setDefaultCard(@Path("id") id: String): Response<CardResponse>

    /** DELETE /cards/{id} — Kartı siler. */
    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): Response<Unit>

    @GET("wallet")
    suspend fun getWalletInfo(): Response<WalletInfoResponse>

    @POST("wallet/topup")
    suspend fun topUpWallet(@Body request: TopUpWalletRequest): Response<WalletInfoResponse>

    @GET("health")
    suspend fun health(): Response<EmptyResponse>

    @GET("customer/ping")
    suspend fun customerPing(): Response<EmptyResponse>

    @GET("admin/ping")
    suspend fun adminPing(): Response<EmptyResponse>

    @GET("admin/licenses")
    suspend fun getAdminLicenses(
        @Query("status") status: String? = null
    ): Response<List<AdminLicenseResponse>>

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
    suspend fun getAdminVehicles(
        @Query("type") type: String? = null,
        @Query("segment") segment: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<VehicleResponse>>

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
    suspend fun deleteAdminVehicle(@Path("id") id: String): Response<EmptyResponse>

    @GET("admin/rentals")
    suspend fun getAdminRentals(
        @Query("status") status: String? = null,
        @Query("userId") userId: String? = null,
        @Query("vehicleId") vehicleId: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<AdminRentalResponse>>

    @GET("admin/rentals/{id}")
    suspend fun getAdminRental(@Path("id") id: String): Response<AdminRentalResponse>

    @GET("admin/locations")
    suspend fun getAdminLocations(): Response<List<VehiclePositionResponse>>

    // ─────────────────────────────────────────────────────────────────────
    // IYZICO — Ödeme altyapısı (aktifleşince hemen kullanıma hazır)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * GET /iyzico/health — Iyzico entegrasyon sağlık kontrolü.
     * Iyzico aktif mi öğrenmek için kullanılabilir.
     */
    @GET("iyzico/health")
    suspend fun iyzicoHealth(): Response<EmptyResponse>

    /**
     * POST /iyzico/payments — Doğrudan kart bilgisiyle ödeme başlatır.
     * Kiralama ödemesi için basketId = "rental-<kiralamaId>" formatında gönderilmeli;
     * ardından POST /rentals/{id}/pay (method: "IYZICO", iyzicoPaymentId: response.paymentId) çağrılmalıdır.
     */
    @POST("iyzico/payments")
    suspend fun createIyzicoPayment(
        @Body request: CreateIyzicoPaymentRequest
    ): Response<IyzicoPaymentResponse>

    /**
     * POST /iyzico/payments/threeds/initialize — 3D Secure akışını başlatır.
     * Yanıttaki threeDSHtmlContentDecoded değeri WebView'a yüklenir.
     */
    @POST("iyzico/payments/threeds/initialize")
    suspend fun initializeThreedsPayment(
        @Body request: CreateIyzicoPaymentRequest
    ): Response<ThreedsInitializeResponse>

    /**
     * POST /iyzico/checkout-form/initialize — Iyzico Checkout Form (WebView) akışını başlatır.
     * Yanıttaki checkoutFormContent WebView'a yüklenir;
     * token ise form tamamlandığında GET /iyzico/checkout-form/result/{token} ile sorgulanır.
     */
    @POST("iyzico/checkout-form/initialize")
    suspend fun initializeCheckoutForm(
        @Body request: InitializeCheckoutFormRequest
    ): Response<CheckoutFormInitializeResponse>

    /**
     * GET /iyzico/checkout-form/result/{token} — Checkout Form sonucunu sorgular.
     * WebView'daki yönlendirme (callbackUrl) tetiklenince bu uç çağrılır.
     */
    @GET("iyzico/checkout-form/result/{token}")
    suspend fun getCheckoutFormResult(
        @Path("token") token: String
    ): Response<IyzicoPaymentResponse>

    /**
     * GET /iyzico/payments/{paymentId} — Ödeme durumunu sorgular.
     * paymentId: IyzicoPaymentResponse.paymentId veya ThreedsInitializeResponse sonrası elde edilir.
     */
    @GET("iyzico/payments/{paymentId}")
    suspend fun getIyzicoPayment(
        @Path("paymentId") paymentId: String
    ): Response<IyzicoPaymentResponse>

    /**
     * POST /iyzico/payments/{paymentId}/cancel — Onaylı ödemeyi iptal eder.
     * reason: DOUBLE_PAYMENT | BUYER_REQUEST | FRAUD | OTHER
     */
    @POST("iyzico/payments/{paymentId}/cancel")
    suspend fun cancelIyzicoPayment(
        @Path("paymentId") paymentId: String,
        @Body request: CancelIyzicoPaymentRequest
    ): Response<IyzicoPaymentResponse>

    /**
     * POST /iyzico/refunds — Kısmi veya tam iade.
     * paymentTransactionId: IyzicoPaymentResponse.paymentTransactionIds listesinden alınır.
     */
    @POST("iyzico/refunds")
    suspend fun refundIyzicoPayment(
        @Body request: RefundIyzicoPaymentRequest
    ): Response<IyzicoPaymentResponse>
}
