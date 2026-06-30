# RenCar Android MVP Mimari Iskeleti

## Karar

Bu proje icin onerilen iskelet:

**Kotlin + Jetpack Compose + MVI + feature-first Clean Architecture**

Buradaki MVP, "Minimum Viable Product" kapsamidir. Compose tabanli yeni bir Android uygulamada Model-View-Presenter deseni tercih edilmemelidir. UI state, event ve tek yonlu veri akisi ihtiyaci nedeniyle MVI daha temiz, test edilebilir ve gelistirmesi daha hizli bir omurga verir.

## Neden MVI

- Tasarim cok adimli akislar iceriyor: onboarding, giris, dogrulama, ehliyet dogrulama, arac secimi, rezervasyon, teslim, aktif kiralama, odeme, cuzdan, kiralamalar ve profil.
- Her ekran icin tek bir `UiState` tanimi loading, content, error, empty ve permission durumlarini netlestirir.
- `Intent -> ViewModel -> UseCase -> Repository -> State/Effect` akisi ekip icinde gorev paylasimini kolaylastirir.
- Compose, `StateFlow` ve immutable state modeli ile MVI'ya dogal sekilde uyar.
- Unit testlerde reducer, use case ve ViewModel davranislari ayri ayri dogrulanabilir.

## Teknoloji Iskeleti

- Dil: Kotlin
- UI: Jetpack Compose, Material 3
- Navigation: Navigation Compose
- DI: Koin
- Async: Coroutines, Flow
- Local state: DataStore
- Local cache: Room, sadece gerekli oldugunda
- Network: Retrofit + OkHttp
- Image loading: Coil
- Harita: MapLibre Native Android
- Test: JUnit, Turbine, MockK, Compose UI Test
- Build kalite: ktlint veya detekt, Gradle build check

## Entegrasyon Kaynaklari

- MapLibre: `https://maplibre.org/`
- API dokumani: `https://rencar.halitkalayci.com/api/docs`
- OpenAPI JSON: `https://rencar.halitkalayci.com/api/docs-json`
- Retrofit base URL: `https://rencar.halitkalayci.com/`
- API path prefix: `/api`

Mevcut API email/parola ile register ve login sagliyor. Tasarimda telefon/OTP gorunuyor; ancak OpenAPI semasinda OTP endpoint'i bulunmadigi icin MVP entegrasyonu email/parola uzerinden ilerlemelidir. Telefon alani `register` request'inde profil bilgisi olarak kullanilabilir. OTP, backend endpoint'i eklendiginde ayri feature olarak ele alinmalidir.

## Modul Stratejisi

MVP icin fazla parcalanmis multi-module yapi yerine tek `app` modulu icinde feature-first paketleme yeterlidir. Proje buyudugunde `core`, `feature:*` ve `data` modullerine ayrilabilir.

Onerilen paket yapisi:

```text
app/src/main/java/com/rencar/app/
  core/
    designsystem/
    navigation/
    model/
    network/
    datastore/
    permission/
    location/
    map/
    util/
  data/
    auth/
    verification/
    vehicle/
    rental/
    profile/
  domain/
    auth/
    verification/
    vehicle/
    rental/
    payment/
    profile/
  feature/
    onboarding/
    auth/
    verification/
    map/
    vehicle/
    reservation/
    delivery/
    activeRental/
    wallet/
    rentals/
    profile/
  MainActivity.kt
```

## Feature Icindeki Standart Dosya Yapisi

Her ekran veya akis icin ayni kucuk iskelet kullanilmali:

```text
feature/example/
  ExampleRoute.kt
  ExampleScreen.kt
  ExampleContract.kt
  ExampleViewModel.kt
  components/
```

`ExampleContract.kt`:

```kotlin
data class ExampleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ExampleIntent {
    data object OnPrimaryClick : ExampleIntent
}

sealed interface ExampleEffect {
    data object NavigateNext : ExampleEffect
}
```

`ExampleViewModel.kt`:

```kotlin
class ExampleViewModel(
    private val useCase: ExampleUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ExampleUiState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ExampleEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: ExampleIntent) {
        when (intent) {
            ExampleIntent.OnPrimaryClick -> submit()
        }
    }

    private fun submit() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching { useCase() }
            .onSuccess { _effect.emit(ExampleEffect.NavigateNext) }
            .onFailure { error ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
    }
}
```

## MVP Kapsami

MVP, tasarimdaki tum ana akislari calisir prototip kalitesinde kapsar:

- Onboarding ve tema destegi
- Email/parola ile kayit ve giris
- Telefon alanini profil/kayit bilgisi olarak toplama
- Ehliyet dogrulama ekranlari
- MapLibre harita uzerinde yakin arac listesi
- Arac detay ve rezervasyon onayi
- Arac teslim kontrol adimlari
- Aktif kiralama takip ekrani
- Yolculuk tamamlandi ve odeme ozeti
- Cuzdan, odeme yontemleri ve islem listesi
- Kiralamalar gecmisi
- Profil ve ayarlar

Backend hazir oldugu icin auth, license, vehicles ve rentals akislarinda once real repository hedeflenmelidir. Gelistirme hizini korumak icin fake repository implementation'lari da tutulabilir; ancak public contract OpenAPI semasiyla uyumlu olmalidir.

## API Kapsami

MVP icin kullanilacak musteri endpointleri:

```text
GET /health
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET /auth/me
POST /license/upload
GET /license/status
GET /vehicles
GET /vehicles/{id}
POST /rentals
GET /rentals
GET /rentals/{id}
POST /rentals/{id}/return
```

Admin endpointleri mobil musteri MVP'sinin disindadir, ancak test verisi uretmek icin backend/admin tarafinda kullanilabilir.

DTO eslesmeleri:

```text
RegisterDto -> email, password, fullName, phone
LoginDto -> email, password
AuthResponseDto -> accessToken, refreshToken, user
LicenseResponseDto -> id, status, image urls, rejectReason
VehicleResponseDto -> id, plate, brand, model, type, pricePerDay, status, latitude, longitude
CreateRentalDto -> vehicleId, endDate
RentalResponseDto -> id, vehicleId, startDate, endDate, totalPrice, status
```

## Navigasyon

Tek `NavHost` kullanilmali. Akislar feature route seviyesinde ayrilmali:

```text
onboarding
auth/login
auth/register
verification/license
home/map
vehicle/detail/{vehicleId}
reservation/summary/{vehicleId}
delivery/checklist/{rentalId}
active-rental/{rentalId}
payment/summary/{rentalId}
wallet
rentals
profile
```

Alt navigasyon MVP'de su tablari tasir:

- Harita
- Cuzdan
- Kiralamalar
- Profil

## State Kurallari

- UI state immutable olmalidir.
- ViewModel icinde Compose state yerine `StateFlow` kullanilmalidir.
- Tek seferlik olaylar navigation, toast, snackbar ve permission request icin `SharedFlow` tabanli `Effect` akisi uzerinden verilmelidir.
- Screen composable sadece state render eder ve user action'i intent olarak yukari yollar.
- Repository response modelleri dogrudan UI'a verilmemelidir; domain veya UI model'e map edilmelidir.

## Tasarim Sistemi

Ilk sprintte minimum tasarim sistemi kurulmalidir:

- `RenCarTheme`
- renk tokenlari: primary blue, success green, warning amber, error red, surface light/dark
- typography tokenlari
- spacing tokenlari
- ortak button, input, sheet, vehicle card, transaction row, empty state, loading state
- light ve dark tema

## Veri Modeli Baslangici

Ana domain modeller:

```text
User
DriverLicenseVerification
Vehicle
VehicleLocation
Reservation
RentalSession
PaymentMethod
WalletTransaction
```

## Katki Kimligi Kurali

- Commit author email: `csezeze@gmail.com`
- Commit mesajlari sade ve proje odakli olmalidir.
- Commit mesajlarinda, PR aciklamalarinda ve dokumanlarda gelistirme araci markasi veya otomatik uretim ibaresi bulunmamalidir.
- Otomatik katki trailer satirlari kullanilmamalidir.
