package com.example.rencar_pair.data.repository

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
import com.example.rencar_pair.domain.repository.RentalRepository
import kotlinx.coroutines.delay
import java.time.Instant

/** Birim testleri ve USE_FAKE_REPOSITORIES modu için sahte implementasyon. */
class FakeRentalRepository : RentalRepository {

    private val uploadedSidesByRental = mutableMapOf<String, Set<RentalPhotoSide>>()
    private var activeRental: ActiveRental? = null
    private val rentals = mutableListOf<Rental>()

    // -------------------------------------------------------------------------
    // Kiralama Oluşturma
    // -------------------------------------------------------------------------

    override suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan?,
        endDate: String?
    ): NetworkResult<Rental> {
        delay(400)
        val rental = Rental(
            id = "fake-rental-${System.currentTimeMillis()}",
            userId = "fake-user",
            vehicleId = vehicleId,
            plan = plan ?: RentalPlan.Daily,
            status = RentalStatus.Preparing,
            paymentStatus = PaymentStatus.Unpaid,
            paymentMethod = null,
            totalPrice = null,
            startFee = 15.0,
            serviceFee = null,
            distanceKm = null,
            durationMinutes = null,
            discountAmount = 0.0,
            startedAt = null,
            endedAt = null,
            scheduledEndDate = null,
            createdAt = Instant.now()
        )
        rentals.add(rental)
        return NetworkResult.Success(rental)
    }

    override suspend fun getMyRentals(): NetworkResult<List<Rental>> {
        delay(250)
        return NetworkResult.Success(rentals.toList())
    }

    override suspend fun getRental(id: String): NetworkResult<Rental> {
        delay(250)
        val rental = rentals.firstOrNull { it.id == id }
            ?: return NetworkResult.Error("Kiralama bulunamadı", 404)
        return NetworkResult.Success(rental)
    }

    // -------------------------------------------------------------------------
    // Aktif Kiralama
    // -------------------------------------------------------------------------

    override suspend fun getActiveRental(): NetworkResult<ActiveRental?> {
        delay(250)
        return NetworkResult.Success(activeRental)
    }

    // -------------------------------------------------------------------------
    // Fotoğraf Operasyonları
    // -------------------------------------------------------------------------

    override suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState> {
        delay(250)
        return NetworkResult.Success(rentalId.toPhotoState())
    }

    override suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState> {
        delay(400)
        val current = uploadedSidesByRental[rentalId].orEmpty()
        uploadedSidesByRental[rentalId] = current + side
        return NetworkResult.Success(rentalId.toPhotoState())
    }

    // -------------------------------------------------------------------------
    // Kiralama Yaşam Döngüsü
    // -------------------------------------------------------------------------

    override suspend fun startRental(rentalId: String): NetworkResult<Unit> {
        delay(500)
        val state = rentalId.toPhotoState()
        return if (state.photosComplete) {
            activeRental = ActiveRental(
                id = rentalId,
                userId = "fake-user",
                vehicleId = "fake-vehicle",
                plan = RentalPlan.PerMinute,
                status = RentalStatus.Active,
                elapsedSeconds = 0.0,
                currentCost = 0.0,
                startedAt = Instant.now(),
                distanceKm = 0.0,
                durationMinutes = 0.0,
                startFee = 15.0,
                createdAt = Instant.now()
            )
            NetworkResult.Success(Unit)
        } else {
            NetworkResult.Error("${state.remainingSides.size} fotoğraf eksik. Önce tüm yönlerden fotoğraf yükleyin.")
        }
    }

    override suspend fun finishRental(rentalId: String): NetworkResult<FinishedRental> {
        delay(600)
        val finished = FinishedRental(
            id = rentalId,
            userId = "fake-user",
            vehicleId = "fake-vehicle",
            plan = RentalPlan.PerMinute,
            totalPrice = 45.50,
            startFee = 15.0,
            serviceFee = 4.5,
            usageFee = 26.0,
            discountAmount = 0.0,
            distanceKm = 3.2,
            durationMinutes = 13.0,
            elapsedSeconds = 780.0,
            paymentStatus = PaymentStatus.Unpaid,
            paymentMethod = null,
            startedAt = Instant.now().minusSeconds(780),
            endedAt = Instant.now(),
            createdAt = Instant.now().minusSeconds(900)
        )
        activeRental = null
        return NetworkResult.Success(finished)
    }

    override suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String?,
        discountCode: String?
    ): NetworkResult<Unit> {
        delay(400)
        if (method == PaymentMethod.Card && cardId.isNullOrBlank()) {
            return NetworkResult.Error("Kart ile ödeme için geçerli bir kart seçilmelidir.")
        }
        return NetworkResult.Success(Unit)
    }

    override suspend fun cancelRental(rentalId: String): NetworkResult<Unit> {
        delay(300)
        return NetworkResult.Success(Unit)
    }

    // -------------------------------------------------------------------------
    // Yardımcılar
    // -------------------------------------------------------------------------

    private fun String.toPhotoState(): RentalPhotosState {
        val uploaded = uploadedSidesByRental[this].orEmpty()
        val remaining = RentalPhotoSide.entries.toSet() - uploaded
        return RentalPhotosState(
            rentalId = this,
            uploadedSides = uploaded,
            remainingSides = remaining,
            photosComplete = remaining.isEmpty()
        )
    }
}
