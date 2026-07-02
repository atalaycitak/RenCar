# RenCar API ve MapLibre Entegrasyon Notları

## Kaynaklar

- MapLibre: `https://maplibre.org/`
- API Docs: `https://rencar.halitkalayci.com/api/docs`
- OpenAPI JSON: `https://rencar.halitkalayci.com/api/docs-json`
- Retrofit base URL: `https://rencar.halitkalayci.com/`
- Retrofit endpoint path'leri OpenAPI şemasındaki gibi `/auth/...`, `/license/...`, `/vehicles/...` şeklinde yazılır.

## Harita Karari

Mobil harita için MapLibre Native Android kullanılacak. Harita feature'ı `core/map` içinde ince bir adapter ile soyutlanmalı; feature ekranları doğrudan MapLibre tiplerine bağlanmamalıdır.

Onerilen sinir:

```text
feature/map -> domain vehicle/rental modelleri
core/map -> MapLibre composable wrapper ve camera/marker modelleri
data/vehicle -> API vehicle response mapper
```

## API Durumu

OpenAPI şemasına göre mevcut müşteri auth akışı kayıt ve parolasız telefon/OTP girişinden oluşur. Bu nedenle MVP:

- kayıt için `POST /auth/register`
- giriş 1. adım için `POST /auth/login`
- giriş 2. adım için `POST /auth/verify-otp`
- oturum yenileme için `POST /auth/refresh`
- profil için `GET /auth/me`

kullanmalıdır.

`LoginDto` yalnızca `phone` alır ve token dönmez; OTP doğrulaması `VerifyOtpDto(phone, code)` ile yapılır. Swagger simülasyonunda varsayılan OTP kodu `123456` olarak görünür.

## Endpoint Gruplari

Auth:

```text
POST /auth/register
POST /auth/login
POST /auth/verify-otp
POST /auth/refresh
POST /auth/logout
GET /auth/me
```

Ehliyet:

```text
POST /license/upload
GET /license/status
```

Araç:

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

- Cüzdan ve ödeme için OpenAPI şemasında endpoint bulunmuyor; bu akışlarda UI ve fake repository korunur.
- Araç lokasyonları `VehicleResponseDto.latitude` ve `VehicleResponseDto.longitude` alanlarından map marker'a çevrilir.
- `GET /admin/locations` müşteri uygulamasında doğrudan kullanılmaz.
- Kiralama baslatmak icin `CreateRentalDto.vehicleId` ve `endDate` yeterlidir.
- Kiralama iade akisi `POST /rentals/{id}/return` ile tamamlanir.
