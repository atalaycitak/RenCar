package com.example.rencar_pair.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rencar_pair.data.remote.RenCarApi
import com.example.rencar_pair.data.remote.dto.ActiveRentalResponse
import com.example.rencar_pair.data.remote.dto.CreateRentalRequest
import com.example.rencar_pair.data.remote.dto.FinishRentalResponse
import com.example.rencar_pair.data.remote.dto.ProcessPaymentRequest
import com.example.rencar_pair.data.remote.dto.RentalVehicleSummaryResponse
import com.example.rencar_pair.data.remote.dto.RentalPhotosStateResponse
import com.example.rencar_pair.data.remote.dto.RentalResponse
import com.example.rencar_pair.data.remote.safeApiCall
import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.FinishedRental
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.PaymentStatus
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.RentalStatus
import com.example.rencar_pair.domain.model.Vehicle
import com.example.rencar_pair.domain.model.VehicleStatus
import com.example.rencar_pair.domain.model.VehicleType
import com.example.rencar_pair.domain.repository.RentalRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.format.DateTimeParseException

private const val TAG = "DefaultRentalRepo"
private const val DEFAULT_LATITUDE = 41.0082
private const val DEFAULT_LONGITUDE = 28.9784

/** Backend'in kabul ettiği MIME tipleri. */
private val ACCEPTED_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png")

class DefaultRentalRepository(
    private val api: RenCarApi,
    private val context: Context
) : RentalRepository {

    // -------------------------------------------------------------------------
    // Kiralama Oluşturma
    // -------------------------------------------------------------------------

    override suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan?,
        endDate: String?
    ): NetworkResult<Rental> = safeApiCall(
        call = {
            api.createRental(
                CreateRentalRequest(
                    vehicleId = vehicleId,
                    plan = plan?.toApiValue(),
                    endDate = endDate
                )
            )
        },
        transform = { it.toDomain() }
    )

    // -------------------------------------------------------------------------
    // Kiralama Listeleme / Detay
    // -------------------------------------------------------------------------

    override suspend fun getMyRentals(): NetworkResult<List<Rental>> = safeApiCall(
        call = { api.getRentals() },
        transform = { list -> list.orEmpty().mapNotNull { it.toDomainOrNull() } }
    )

    override suspend fun getRental(id: String): NetworkResult<Rental> = safeApiCall(
        call = { api.getRental(id) },
        transform = { it.toDomain() }
    )

    // -------------------------------------------------------------------------
    // Aktif Kiralama
    // -------------------------------------------------------------------------

    override suspend fun getActiveRental(): NetworkResult<ActiveRental?> {
        return when (val result = safeApiCall(
            call = { api.getActiveRental() },
            transform = { it.toDomain() }
        )) {
            is NetworkResult.Success -> NetworkResult.Success(result.data)
            is NetworkResult.Error -> if (result.code == 404) {
                NetworkResult.Success(null)
            } else {
                result
            }
        }
    }

    // -------------------------------------------------------------------------
    // Fotoğraf Operasyonları
    // -------------------------------------------------------------------------

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> =
        safeApiCall(
            call = { api.getRentalPhotos(rentalId) },
            transform = { it.toDomain() }
        )

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        val filePart = photoUri.toMultipartPart()
            ?: return NetworkResult.Error("Fotoğraf dosyası okunamadı. Lütfen tekrar deneyin.")

        val sidePart = side.toApiValue().toRequestBody("text/plain".toMediaTypeOrNull())

        return safeApiCall(
            call = { api.uploadRentalPhoto(rentalId, sidePart, filePart) },
            transform = { it.toDomain() }
        )
    }

    // -------------------------------------------------------------------------
    // Kiralama Yaşam Döngüsü
    // -------------------------------------------------------------------------

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> = safeApiCall(
        call = { api.startRental(rentalId) },
        transform = { Unit }
    )

    override suspend fun finishRental(rentalId: String): NetworkResult<FinishedRental> =
        safeApiCall(
            call = { api.finishRental(rentalId) },
            transform = { it.toDomain() }
        )

    override suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String?,
        discountCode: String?
    ): NetworkResult<Unit> {
        if (method == PaymentMethod.Card && cardId.isNullOrBlank()) {
            return NetworkResult.Error("Kart ile ödeme için geçerli bir kart seçilmelidir.")
        }
        return safeApiCall(
            call = {
                api.processPayment(
                    id = rentalId,
                    request = ProcessPaymentRequest(
                        method = method.toApiValue(),
                        cardId = cardId,
                        discountCode = discountCode
                    )
                )
            },
            transform = { Unit }
        )
    }

    override suspend fun cancelRental(rentalId: String): NetworkResult<Unit> {
        return try {
            val response = api.cancelRental(rentalId)
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error("Kiralama iptal edilemedi.", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error("Kiralama iptal sırasında bir hata oluştu.")
        }
    }

    // -------------------------------------------------------------------------
    // Dönüşüm Yardımcıları
    // -------------------------------------------------------------------------

    private fun String.toMultipartPart(): MultipartBody.Part? {
        return runCatching {
            val uri = Uri.parse(this)
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            if (mimeType.lowercase() !in ACCEPTED_MIME_TYPES) {
                Log.w(TAG, "Kabul edilmeyen MIME tipi: $mimeType")
                return null
            }

            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "rental-photo.jpg"

            MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )
        }.getOrElse {
            Log.e(TAG, "Fotoğraf okuma hatası", it)
            null
        }
    }

    private fun RentalPhotosStateResponse.toDomain(): RentalPhotosState {
        val uploaded = photos.mapNotNull { RentalPhotoSide.fromApiString(it.side) }.toSet()
        val remaining = remainingSides.mapNotNull { RentalPhotoSide.fromApiString(it) }.toSet()
        return RentalPhotosState(
            rentalId = rentalId,
            uploadedSides = uploaded,
            remainingSides = remaining,
            photosComplete = photosComplete
        )
    }

    private fun RentalResponse.toDomain(): Rental = Rental(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        vehicle = vehicle?.toRentedVehicle(),
        plan = RentalPlan.fromApiString(plan),
        status = RentalStatus.fromApiString(status),
        paymentStatus = PaymentStatus.fromApiString(paymentStatus),
        paymentMethod = PaymentMethod.fromApiString(paymentMethod),
        totalPrice = totalPrice,
        startFee = startFee,
        serviceFee = serviceFee,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        discountAmount = discountAmount,
        startedAt = (startedAt ?: startDate)?.parseInstantOrNull(),
        endedAt = endedAt?.parseInstantOrNull(),
        scheduledEndDate = endDate?.parseInstantOrNull(),
        createdAt = Instant.parse(createdAt)
    )

    private fun RentalVehicleSummaryResponse.toRentedVehicle(): Vehicle {
        return Vehicle(
            id = id,
            plate = plate,
            brand = brand,
            model = model,
            type = VehicleType.fromApiString(type),
            pricePerDay = 0.0,
            status = VehicleStatus.Rented,
            latitude = DEFAULT_LATITUDE,
            longitude = DEFAULT_LONGITUDE,
            locationName = "Aktif kiralama",
            canReserve = false,
            canUnlock = false
        )
    }

    private fun RentalResponse.toDomainOrNull(): Rental? = runCatching { toDomain() }.getOrElse {
        Log.w(TAG, "Kiralama kaydı atlandı (id=$id): ${it.message}")
        null
    }

    private fun ActiveRentalResponse.toDomain(): ActiveRental = ActiveRental(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        plan = RentalPlan.fromApiString(plan),
        status = RentalStatus.fromApiString(status),
        elapsedSeconds = elapsedSeconds ?: 0.0,
        currentCost = currentCost ?: 0.0,
        startedAt = (startedAt ?: startDate)?.parseInstantOrNull(),
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        startFee = startFee,
        createdAt = Instant.parse(createdAt)
    )

    private fun FinishRentalResponse.toDomain(): FinishedRental = FinishedRental(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        plan = RentalPlan.fromApiString(plan),
        totalPrice = totalPrice,
        startFee = startFee,
        serviceFee = serviceFee,
        usageFee = usageFee,
        discountAmount = discountAmount,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes,
        elapsedSeconds = elapsedSeconds,
        paymentStatus = PaymentStatus.fromApiString(paymentStatus),
        paymentMethod = PaymentMethod.fromApiString(paymentMethod),
        startedAt = (startedAt ?: startDate)?.parseInstantOrNull(),
        endedAt = endedAt?.parseInstantOrNull(),
        createdAt = Instant.parse(createdAt)
    )

    private fun String.parseInstantOrNull(): Instant? = try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        Log.w(TAG, "Geçersiz tarih formatı: $this")
        null
    }
}
