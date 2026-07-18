package com.example.rencar_pair.domain.repository

import com.example.rencar_pair.domain.NetworkResult
import com.example.rencar_pair.domain.model.ActiveRental
import com.example.rencar_pair.domain.model.FinishedRental
import com.example.rencar_pair.domain.model.PaymentMethod
import com.example.rencar_pair.domain.model.Rental
import com.example.rencar_pair.domain.model.RentalPhotoSide
import com.example.rencar_pair.domain.model.RentalPhotosState
import com.example.rencar_pair.domain.model.RentalPlan

interface RentalRepository {

    /** POST /rentals — Yeni bir kiralama oluşturur (PREPARING durumunda başlar). */
    suspend fun createRental(
        vehicleId: String,
        plan: RentalPlan? = null,
        endDate: String? = null
    ): NetworkResult<Rental>

    /** GET /rentals — Kullanıcının tüm kiralamalarını döner. */
    suspend fun getMyRentals(): NetworkResult<List<Rental>>

    /** GET /rentals/{id} — Tek bir kiralama detayını döner. */
    suspend fun getRental(id: String): NetworkResult<Rental>

    /**
     * GET /rentals/active — Aktif yolculuğu döner.
     * API 404 döndürürse Success(null) olarak işlenir (aktif kiralama yok).
     */
    suspend fun getActiveRental(): NetworkResult<ActiveRental?>

    /**
     * GET /rentals/{id}/photos — Fotoğraf durumunu döner.
     * 0/4, 1/4, 4/4 sayacı ve hangi yönlerin yüklendiği bilgisini içerir.
     */
    suspend fun getPreparationPhotos(rentalId: String): NetworkResult<RentalPhotosState>

    /**
     * POST /rentals/{id}/photos — Tek yön fotoğrafı yükler.
     * Aynı yöne ikinci yükleme öncekinin üzerine yazar.
     * HEIC/WebP MIME tipinde dosya gelirse hata döner.
     */
    suspend fun uploadPreparationPhoto(
        rentalId: String,
        side: RentalPhotoSide,
        photoUri: String
    ): NetworkResult<RentalPhotosState>

    /**
     * POST /rentals/{id}/start — Kiralamayı başlatır.
     * Yalnızca 4 fotoğraf tamamlandıktan (photosComplete=true) sonra çağrılmalıdır.
     */
    suspend fun startRental(rentalId: String): NetworkResult<Unit>

    /**
     * POST /rentals/{id}/finish — Aktif kiralamayı bitirir.
     * Yanıtta ödeme ekranına taşınacak fiyat kırılımı bulunur.
     */
    suspend fun finishRental(rentalId: String): NetworkResult<FinishedRental>

    /**
     * POST /rentals/{id}/pay — Ödeme işlemini gerçekleştirir.
     * CARD yöntemi seçilirse [cardId] zorunludur.
     */
    suspend fun payRental(
        rentalId: String,
        method: PaymentMethod,
        cardId: String? = null,
        discountCode: String? = null
    ): NetworkResult<Unit>

    /** DELETE /rentals/{id} — PREPARING aşamasındaki kiralamayı iptal eder. */
    suspend fun cancelRental(rentalId: String): NetworkResult<Unit>
}
