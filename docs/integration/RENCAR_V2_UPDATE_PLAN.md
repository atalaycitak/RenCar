# RenCar v2 API ve Canli Konum Entegrasyon Plani

Bu dokuman, RenCar Android uygulamasinin `https://rencarv2.halitkalayci.com` uzerindeki v2 API'ye gecisi, derste gosterilen WebSocket/Socket.IO canli konum akisi ve ekran akislari dikkate alinarak hazirlandi.

## Kaynaklar

- v2 Swagger: https://rencarv2.halitkalayci.com/api/docs
- v2 OpenAPI JSON: https://rencarv2.halitkalayci.com/api/openapi.json
- Socket.IO istemci ornegi: https://gist.github.com/halitkalayci/4395065e3be8ac9e4ff1bacb98ff9c56
- Incelenen ders ekranlari: Swagger endpoint listesi, WebSocket anlatimi, ana harita, aktif yolculuk, odeme, cuzdan ve kiralama gecmisi ekranlari.

## Genel Sonuc

Mevcut mimari v2 gecisi icin uygun durumda: Retrofit tabanli `RenCarApi`, repository katmani, use case'ler, MVI state/viewmodel yapisi ve MapLibre bileseni zaten var. Ancak v2 API sozlesmesi onceki endpoint ve modellerden farkli oldugu icin uygulamada kontrollu bir yeniden esleme gerekiyor.

En kritik degisiklikler sunlar:

- Base URL v2 ortama alinacak: `https://rencarv2.halitkalayci.com/`
- Odeme endpointleri eski `payments/...` yapisindan v2'deki `cards`, `wallet`, `rentals/{id}/pay` yapisina tasinacak.
- Rezervasyon gercek endpointlere baglanacak: `POST /reservations`, `GET /reservations/active`, `DELETE /reservations/{id}`.
- Kiralama akisi v2'de ikiye ayriliyor:
  - `PER_MINUTE` ve `HOURLY`: once `PREPARING`, sonra 4 yon arac fotografi, sonra `start`, sonra `finish`, sonra `pay`.
  - `DAILY`: geriye uyumlu eski akis; `endDate` ile dogrudan aktif baslayabiliyor.
- Canli konum icin mevcut OkHttp WebSocket denemesi yerine Socket.IO client kullanilmali.
- Harita sadece statik marker gostermemeli; Socket.IO'dan gelen arac konumu, aktif yolculuk ve durum paneli ile birlikte guncellenmeli.

## Ekran Goruntulerinden Cikan Akislar

| Alan | Derste gorulen durum | Uygulamada yapilacak is |
| --- | --- | --- |
| Ana harita | Harita uzerinde arac marker'lari, kullanici konumu, filtreler ve "En Yakin Araci Bul" butonu var. | `GET /vehicles?includeBusy=true` ile musait ve kullanimdaki araclar cekilecek; status'a gore renk/aktiflik ayrilacak. |
| Rezervasyon karti | "Rezervasyon - Renault Clio", kalan sure ve "Kilidi Ac" butonu gorunuyor. | `POST /reservations` sonrasi aktif rezervasyon state'i tutulacak; `remainingSeconds` UI'da sayacak. |
| Aktif yolculuk | Sure, mesafe, anlik ucret, arac karti ve kucuk harita var. | `GET /rentals/active` ile `elapsedSeconds`, `currentCost`, `distanceKm` alanlari okunacak; Socket.IO konumu haritaya islenecek. |
| Arac fotograf akisi | PER_MINUTE/HOURLY planda yolculuk baslamadan 4 yon fotograf gerekiyor. | `POST /rentals/{id}/photos` ve `GET /rentals/{id}/photos` icin kamera/yukleme adimi eklenecek. |
| Odeme | Yolculuk tamamlandi, ucret kalemleri, cuzdan/kart secimi, indirim kodu ve odeme butonu var. | `POST /rentals/{id}/finish` sonucundaki ucret dokumu odeme ekranina tasinacak; `POST /rentals/{id}/pay` ile odeme alinacak. |
| Cuzdan | Bakiye, bakiye yukleme, kartlar ve son islemler gorunuyor. | `GET /wallet`, `POST /wallet/topup`, `GET /cards`, `POST /cards`, `PATCH /cards/{id}/default`, `DELETE /cards/{id}` gercek endpointlere baglanacak. |
| Kiralama gecmisi | Bu ayki yolculuk sayisi/harcama ve gecmis liste var. | `GET /rentals` ve `GET /rentals/stats` ile liste ve aylik ozet ayrilacak. |
| Swagger/WebSocket | Socket.IO namespace ve event mantigi anlatildi. | Socket.IO dependency eklenecek; auth token handshake ile `/ws/locations` namespace'ine baglanilacak. |

## v2 Endpoint Esleme Plani

### Auth

Mevcut auth akisi v2 ile buyuk oranda uyumlu.

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/verify-otp`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

Dikkat edilecekler:

- Login parolasiz ve iki adimli: once telefon ile OTP istenir, sonra `verify-otp`.
- Varsayilan OTP simule kodu dokumanda `123456` olarak belirtiliyor.
- Refresh token rotation var; access token suresi dolarsa refresh edip yeni tokenlar saklanmali.
- Ehliyet onayi sonrasi role `PENDING` -> `CUSTOMER` olur; uygulama refresh veya yeniden `me` cekerek rol degisimini almali.

### License

v2'de ehliyet yukleme artik 3 parca bekliyor:

- `front`
- `back`
- `selfie`

Mevcut `uploadLicense(front, back)` imzasi degismeli.

Yapilacaklar:

- `RenCarApi.uploadLicense(front, back, selfie)` olarak guncellenecek.
- License domain modeline `NOT_SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED` durumlari net eklenecek.
- Reddedilme nedeni varsa UI'da gosterilecek.
- Selfie adimi tasarimdaki ucuncu dogrulama adimi olarak eklenmeli.

### Vehicles

v2 arac modeli zenginlesti:

- `pricePerDay`
- `pricePerMinute`
- `pricePerHour`
- `fuelPercent`
- `rangeKm`
- `transmission`
- `seats`
- `segment`
- `status`
- `latitude`
- `longitude`
- `updatedAt`

Mevcut uygulamada `fuelLevelPercent`, `seatCount`, `locationUpdatedAt` gibi alanlar var; v2 DTO ile isim eslemesi duzeltilmeli.

Endpointler:

- `GET /vehicles`
- `GET /vehicles/{id}`
- `GET /vehicles/{id}/quote`

Yapilacaklar:

- `GET /vehicles` sorgusuna `segment` ve `includeBusy` eklenecek.
- Ana haritada `includeBusy=true` kullanilarak `RESERVED` ve `RENTED` araclar da gri/pasif marker olarak gosterilecek.
- `MAINTENANCE` araclarsa musteride hic gosterilmeyecek.
- `GET /vehicles/{id}/quote?plan=PER_MINUTE&minutes=30` ile arac detayinda tahmini ucret gosterilecek.
- `VehicleStatus` enum'una `Reserved` eklenmeli; su an sadece `Available`, `Rented`, `Maintenance`, `Unknown` var.

### Reservations

v2 ile rezervasyon ayri bir kaynak haline gelmis durumda.

Endpointler:

- `POST /reservations`
- `GET /reservations/active`
- `DELETE /reservations/{id}`

Yapilacaklar:

- `ReservationRepository` gercek endpointlere baglanacak.
- `Reservation` domain modeli eklenecek veya mevcut rental tabanli gecici modelden ayrilacak.
- Ana haritada rezerve edilen araca ait kart gosterilecek.
- `remainingSeconds` ile 15 dakikalik sure UI'da saydirilacak.
- Rezervasyon yoksa `Kilidi Ac` butonu pasif, rezervasyon varsa aktif olacak.
- Rezervasyon iptalinde marker ve buton state'i aninda yenilenecek.

### Rentals

v2 kiralama akisi daha detayli ve ders ekranlariyla uyumlu.

Endpointler:

- `POST /rentals`
- `GET /rentals`
- `GET /rentals/stats`
- `GET /rentals/active`
- `POST /rentals/{id}/photos`
- `GET /rentals/{id}/photos`
- `POST /rentals/{id}/start`
- `DELETE /rentals/{id}`
- `GET /rentals/{id}`
- `POST /rentals/{id}/finish`
- `POST /rentals/{id}/return`
- `POST /rentals/{id}/pay`

Yapilacaklar:

- `RentalStatus` enum'u v2'ye gore netlestirilecek: `PREPARING`, `ACTIVE`, `COMPLETED`, `CANCELLED`.
- `PaymentStatus` eklenecek: `UNPAID`, `PAID`.
- `RentalPlan` eklenecek: `PER_MINUTE`, `HOURLY`, `DAILY`.
- `PER_MINUTE` ve `HOURLY` icin:
  1. Aktif rezervasyon kontrol edilir.
  2. `POST /rentals` ile `PREPARING` olusturulur.
  3. 4 yon fotograf yuklenir: `FRONT`, `BACK`, `LEFT`, `RIGHT`.
  4. `POST /rentals/{id}/start` ile yolculuk baslar.
  5. Aktif yolculuk ekrani `GET /rentals/active` ve Socket.IO ile guncellenir.
  6. `POST /rentals/{id}/finish` ile ucret hesaplanir.
  7. Odeme ekrani `POST /rentals/{id}/pay` ile tamamlanir.
- `DAILY` icin eski `return` akisi korunabilir ama yeni ekranda ana oncelik dakika/saatlik akis olmali.

### Wallet ve Cards

Mevcut kodda eski endpointler var:

- `payments/cards`
- `wallet/balance`
- `wallet/top-up`

v2'de bunlar degisti:

- `GET /wallet`
- `POST /wallet/topup`
- `GET /cards`
- `POST /cards`
- `PATCH /cards/{id}/default`
- `DELETE /cards/{id}`

Yapilacaklar:

- `RenCarApi` endpointleri v2 isimlerine cekilecek.
- `PaymentRepository` ve `WalletRepository` fallback mantigi korunabilir ama real mode'da v2 endpointleri kullanilmali.
- Kart modeli gercek kart bilgisi saklamiyor; sadece `brand`, `last4`, `expMonth`, `expYear`, `isDefault`.
- Odeme artik `payments/process` ile degil, `POST /rentals/{id}/pay` ile yapilacak.
- Cuzdan ekrani `GET /wallet` ile bakiye ve son 20 islemi tek seferde alacak.

## Socket.IO Canli Konum Plani

Dersteki ornege gore canli konum standart HTTP WebSocket degil, Socket.IO ile kuruluyor.

### Sozlesme

- Namespace: `/ws/locations`
- Customer eventi: `my-vehicle`
- Admin eventi: `vehicle-positions`
- Auth: Socket.IO handshake icinde `auth = { token: accessToken }`
- Payload ornegi:

```json
{
  "ts": "2026-07-14T16:00:00.000Z",
  "vehicle": {
    "vehicleId": "clx0veh1234567890",
    "latitude": 41.0151,
    "longitude": 28.9795
  }
}
```

### Android tarafinda yapilacaklar

- `io.socket:socket.io-client` dependency'si eklenecek.
- Mevcut `DefaultVehicleLocationRepository` OkHttp WebSocket yerine Socket.IO client ile degistirilecek.
- Token `TokenStore` uzerinden alinacak.
- `EVENT_CONNECT_ERROR` gelirse bir kez refresh denenip yeniden baglanilacak.
- Aktif yolculuk yoksa event gelmeyebilir; bu durumda UI sessiz kalmali, hata gibi davranmamali.
- `my-vehicle` sadece musteri aktif yolculugundaki araci verir; ana haritadaki tum araclar icin admin snapshot veya `GET /vehicles?includeBusy=true` kullanilmali.
- Admin panel yapilacaksa `vehicle-positions` eventi ayri bir admin client olarak tasarlanmali.

### Mevcut koddan farki

Bugunku kodda `BuildConfig.VEHICLE_LOCATION_WS_URL` ile OkHttp `newWebSocket` kullaniliyor. v2'de bu yeterli degil; Socket.IO protokolu handshake, namespace ve event katmani istiyor. Bu yuzden:

- `VEHICLE_LOCATION_WS_URL` yerine `BASE_URL + "/ws/locations"` mantigi kullanilacak.
- `VehicleLocationStreamMode.WebSocket` adi korunabilir ama implementasyon Socket.IO olacak.
- UI'daki canli harita durum paneli korunacak; sadece stream kaynagi gercek Socket.IO'a baglanacak.

## UI Akislarinda Degisecek Yerler

### Home / Map

- Filtreler `segment` ile backend'e baglanacak: `ECONOMY`, `COMFORT`, `SUV`.
- Haritada `RESERVED` ve `RENTED` araclar gri/pasif gosterilecek.
- Secili arac kartinda fiyat plani, tahmini ucret ve rezervasyon uygunlugu gosterilecek.
- `Rezerve Et` ve `Kilidi Ac` butonlari ayri state ile yonetilecek:
  - Arac `AVAILABLE` ise `Rezerve Et` aktif.
  - Aktif rezervasyon varsa `Kilidi Ac` aktif.
  - Arac `RENTED/RESERVED` ise kullaniciya ait degilse pasif.

### Reservation

- Aktif rezervasyon karti ana harita uzerinde gosterilecek.
- Kalan sure `remainingSeconds` ile baslayip lokal timer ile azaltilacak.
- Sure bitince `GET /reservations/active` tekrar cekilecek.

### Rental Preparation

- `PREPARING` durumunda 4 yon fotograf ekrani acilacak.
- Her fotograf `POST /rentals/{id}/photos` ile yuklenecek.
- `GET /rentals/{id}/photos` ile eksik yonler listelenecek.
- 4 fotograf tamamlanmadan `start` butonu pasif olacak.

### Active Rental

- `GET /rentals/active` ana veri kaynagi olacak.
- `elapsedSeconds`, `currentCost`, `distanceKm` API'den geldigi icin lokal hesaplama sadece destekleyici olacak.
- Socket.IO `my-vehicle` geldikce kucuk haritadaki marker hareket edecek.
- "Kilitle / Ac" butonu su an backend sozlesmesinde ayri endpoint olarak gorunmuyor; UI'da simule/placeholder oldugu acik tutulmali veya kapsam disi birakilmali.

### Finish and Payment

- `finish` sonrasi odeme ekrani acilacak.
- Ucret kalemleri:
  - `usageFee`
  - `startFee`
  - `serviceFee`
  - `discountAmount`
  - `totalPrice`
- Cuzdan veya kart secimine gore `POST /rentals/{id}/pay` cagrilacak.
- Odeme basariliysa history/cuzdan ekranlari yenilenecek.

### Wallet / Cards / History

- Cuzdan bakiyesi ve hareketleri `GET /wallet` ile tek yerden cekilecek.
- Bakiye yukleme `POST /wallet/topup`.
- Kart ekleme, varsayilan yapma ve silme v2 endpointleri ile calisacak.
- History listesi `GET /rentals`, aylik ozet `GET /rentals/stats`.

## Uygulama Sirasina Gore Is Listesi

### 1. v2 API temel gecis

- Base URL'i v2'ye tasima.
- `docs/api/openapi.json` dosyasini v2 OpenAPI ile guncelleme.
- `RenCarApi` endpoint isimlerini v2'ye gore duzeltme.
- DTO-domain mapper'larini v2 alan adlariyla esleme.
- Smoke test: `health`, login, verify-otp, `auth/me`.

### 2. Vehicles + reservations

- `VehicleStatus.Reserved`, `segment`, `pricePerHour`, `fuelPercent`, `seats` alanlarini domain'e ekleme.
- `GET /vehicles` icin `segment` ve `includeBusy` query parametreleri.
- `GET /vehicles/{id}/quote` icin quote repository/use case.
- Reservation repository'nin v2 endpointlere baglanmasi.
- Ana harita buton state'lerinin reservation state'e gore ayrilmasi.

### 3. Rental lifecycle

- `RentalPlan`, `RentalStatus`, `PaymentStatus` modellerini v2'ye gore duzeltme.
- `POST /rentals`, `GET /rentals/active`, `POST /rentals/{id}/start`, `finish`, `pay` endpointleri.
- 4 yon fotograf yukleme ekraninin eklenmesi.
- Aktif yolculuk ekraninda API'den gelen sure/ucret/mesafe alanlarinin kullanilmasi.

### 4. Wallet, cards, payment

- Eski payment endpointlerinin kaldirilmasi veya deprecated hale getirilmesi.
- `cards` ve `wallet` endpointlerinin baglanmasi.
- Odeme ekraninin `rentals/{id}/pay` ile calismasi.
- Cuzdan ve history ekranlarinin odeme sonrasi yenilenmesi.

### 5. Socket.IO live map

- Socket.IO dependency eklenmesi.
- `RideLocationClient` benzeri bir client'in mevcut paket yapisina uyarlanmasi.
- Token refresh ve reconnect davranisinin test edilmesi.
- Aktif yolculuk haritasinda `my-vehicle` event'i ile marker guncelleme.
- Ana haritada snapshot + status temelli marker guncelleme.

## Test Plani

### Unit test

- Vehicle mapper v2 alanlari: `fuelPercent`, `seats`, `segment`, `pricePerHour`, `updatedAt`.
- Reservation state: aktif rezervasyon varsa `Kilidi Ac` aktif, yoksa pasif.
- Rental lifecycle: `PREPARING -> ACTIVE -> COMPLETED -> PAID`.
- Wallet/card mapper testleri.
- Socket.IO parse testi: `my-vehicle` payload'i `VehiclePoint` modeline donusmeli.

### Integration/smoke test

- `GET /health`
- Login + verify OTP + token saklama.
- `GET /auth/me`
- `GET /vehicles?includeBusy=true`
- `POST /reservations`
- `GET /reservations/active`
- `POST /rentals`
- `POST /rentals/{id}/photos`
- `POST /rentals/{id}/start`
- `GET /rentals/active`
- Socket.IO `my-vehicle` akisi.
- `POST /rentals/{id}/finish`
- `POST /rentals/{id}/pay`
- `GET /wallet`
- `GET /rentals/stats`

### Android dogrulama komutlari

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebug "-Prencar.repositoryMode=fake"
```

## Riskler ve Notlar

- v2 endpointleri CUSTOMER rol istiyor; PENDING kullanici ile vehicle/rental endpointleri 403 donebilir. Test kullanicisinda ehliyet onayi veya CUSTOMER token gerekli.
- Socket.IO, OkHttp WebSocket ile birebir ayni degil. Sadece URL degistirmek calistirmaya yetmez.
- Aktif yolculuk yoksa `my-vehicle` event'i hic gelmeyebilir; UI bunu hata gibi gostermemeli.
- `startDate` v2'de deprecated; yeni kodda `startedAt` kullanilmali.
- `return` endpointi eski DAILY akis icin korunuyor; dakika/saatlik yeni akista `finish` ve `pay` esas alinmali.
- Kart endpointleri gercek kart numarasi saklamiyor; yalnizca gorsel/simule kart metasi var.
- Ders ekranlarindaki "Kilitle / Ac" icin v2 OpenAPI'de ayri bir lock endpointi gorunmuyor. Bu ozellik backend gelene kadar UI state veya demo davranisi olarak sinirli tutulmali.

## Basari Kriterleri

- Uygulama v2 base URL ile login olup CUSTOMER token alabiliyor.
- Harita v2 arac listesini status ve segment bilgisiyle gosterebiliyor.
- Rezerve etme ve kilidi acma butonlari ayni anda degil, gercek state'e gore ayri calisiyor.
- Dakika/saatlik kiralama PREPARING, fotograf, start, active, finish, pay akisini tamamliyor.
- Aktif yolculuk ekraninda sure, mesafe ve anlik ucret API verisiyle guncelleniyor.
- Socket.IO `my-vehicle` event'i geldikce aktif yolculuk haritasi canli hareket ediyor.
- Cuzdan, kartlar, odeme ve kiralama gecmisi v2 endpointlerinden veri aliyor.
- Fake repository modu bozulmadan demo icin calismaya devam ediyor.
