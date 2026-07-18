package com.example.rencar_pair.domain.usecase

import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.RentalPlan
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.repository.RentalRepository
import com.example.rencar_pair.domain.repository.ReservationRepository

/**
 * Kiralama akışının tüm domain operasyonlarını tek noktadan sunan use case sınıfı.
 * Reservation operasyonları (oluşturma, iptal) burada tutulur çünkü
 * rezervasyon → kiralama akışı tek bir ViewModel üzerinden yürütülür.
 */
class RentalUseCases(
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository
) {
    // --- Rezervasyon Operasyonları ---

    suspend fun createReservation(vehicleId: String) =
        reservationRepository.createReservation(vehicleId)

    suspend fun getActiveReservation() =
        reservationRepository.getActiveReservation()

    suspend fun cancelReservation(id: String) =
        reservationRepository.cancelReservation(id)

    // --- Kiralama Operasyonları ---

    /** POST /rentals — Yeni kiralama oluşturur (PREPARING). */
    suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan? = null,
        endDate: String? = null
    ) = rentalRepository.createRental(vehicleId, plan, endDate)

    /** GET /rentals/active — Aktif yolculuğu döner; yoksa null. */
    suspend fun getActiveRental() = rentalRepository.getActiveRental()

    /** GET /rentals — Tüm kiralamalar. */
    suspend fun getMyRentals() = rentalRepository.getMyRentals()

    /** GET /rentals/{id} */
    suspend fun getRental(id: String) = rentalRepository.getRental(id)

    // --- Fotoğraf Operasyonları ---

    /** GET /rentals/{id}/photos — Fotoğraf durumu. */
    suspend fun getPreparationPhotos(rentalId: String) =
        rentalRepository.getPreparationPhotos(rentalId)

    /**
     * POST /rentals/{id}/photos — Tek yön fotoğraf yükleme.
     * HEIC/WebP MIME tipi gelirse domain katmanında hata üretilir.
     */
    suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ) = rentalRepository.uploadPreparationPhoto(rentalId, side, photoUri)

    // --- Kiralama Yaşam Döngüsü ---

    /**
     * POST /rentals/{id}/start — Yalnızca photosComplete=true iken çağrılmalıdır.
     * Bu kontrolü yapmamanız durumunda backend 400 döner.
     */
    suspend fun startRental(rentalId: String) = rentalRepository.startRental(rentalId)

    /** POST /rentals/{id}/finish — Aktif kiralamayı bitirir; ödeme ekranı için fiyat kırılımı döner. */
    suspend fun finishRental(rentalId: String) = rentalRepository.finishRental(rentalId)

    /**
     * POST /rentals/{id}/pay — Ödeme gerçekleştirir.
     * CARD yöntemi seçildiyse [cardId] boş bırakılamaz.
     */
    suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null,
        discountCode: String? = null
    ) = rentalRepository.payRental(rentalId, method, cardId, discountCode)

    /** DELETE /rentals/{id} — PREPARING aşamasındaki kiralamayı iptal eder. */
    suspend fun cancelRental(rentalId: String) = rentalRepository.cancelRental(rentalId)
}
