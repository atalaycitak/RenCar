# RenCar API ve MapLibre Entegrasyon Notlari

## Kaynaklar

- MapLibre: `https://maplibre.org/`
- API Docs: `https://rencar.halitkalayci.com/api/docs`
- OpenAPI JSON: `https://rencar.halitkalayci.com/api/docs-json`
- Retrofit base URL: `https://rencar.halitkalayci.com/`
- API path prefix: `/api`

## Harita Karari

Mobil harita icin MapLibre Native Android kullanilacak. Harita feature'i `core/map` icinde ince bir adapter ile soyutlanmali; feature ekranlari dogrudan MapLibre tiplerine baglanmamalidir.

Onerilen sinir:

```text
feature/map -> domain vehicle/rental modelleri
core/map -> MapLibre composable wrapper ve camera/marker modelleri
data/vehicle -> API vehicle response mapper
```

## API Durumu

OpenAPI semasina gore mevcut musteri akislarinda email/parola auth var. Tasarimda telefon/OTP gorunse de OTP endpoint'i yok. Bu nedenle MVP:

- kayit icin `POST /auth/register`
- giris icin `POST /auth/login`
- oturum yenileme icin `POST /auth/refresh`
- profil icin `GET /auth/me`

kullanmalidir.

Telefon alani `RegisterDto.phone` uzerinden kayit bilgisidir. Telefonla OTP girisi, backend endpoint'i eklenene kadar backlog'da tutulur.

## Endpoint Gruplari

Auth:

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET /auth/me
```

Ehliyet:

```text
POST /license/upload
GET /license/status
```

Arac:

```text
GET /vehicles
GET /vehicles/{id}
```

Kiralama:

```text
POST /rentals
GET /rentals
GET /rentals/{id}
POST /rentals/{id}/return
```

## Android Data Katmani

Onerilen Retrofit servisleri:

```text
AuthApi
LicenseApi
VehiclesApi
RentalsApi
```

DI:

- Retrofit, OkHttp, repository ve use case bagimliliklari Koin module'leri uzerinden saglanir.
- ViewModel'lar Koin `viewModel` DSL'i ile baglanir.

Token yonetimi:

- `accessToken` ve `refreshToken` DataStore icinde saklanir.
- OkHttp interceptor `Authorization: Bearer <token>` header'ini ekler.
- 401 durumunda refresh token akisi tek bir auth repository noktasindan calisir.
- Logout hem API'ye istek atar hem lokal tokenlari temizler.

## MVP Notlari

- Cuzdan ve odeme icin OpenAPI semasinda endpoint bulunmuyor; bu akislarda UI ve fake repository korunur.
- Arac lokasyonlari `VehicleResponseDto.latitude` ve `VehicleResponseDto.longitude` alanlarindan map marker'a cevrilir.
- `GET /admin/locations` musteri uygulamasinda dogrudan kullanilmaz.
- Kiralama baslatmak icin `CreateRentalDto.vehicleId` ve `endDate` yeterlidir.
- Kiralama iade akisi `POST /rentals/{id}/return` ile tamamlanir.
