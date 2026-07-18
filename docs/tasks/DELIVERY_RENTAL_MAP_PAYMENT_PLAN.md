# RenCar Teslim Oncesi Kiralama, Harita ve Odeme Plani

Bu dokumanin amaci, teslim oncesi en kritik eksikleri iki kisi arasinda cakisma yaratmadan bolmek ve uygulamayi hocanin bakacagi senaryoya hazir hale getirmektir.

Ana kriter: API v2 degisiklikleri uygulamanin her katmanini dagitmamali. Retrofit/DTO degisiklikleri data katmaninda kalmali, domain use case'ler ve MVI state'leri ekranlara temiz veri tasimali. Harita da sadece ana sayfaya gomulu bir parca gibi kalmamali; aktif kiralama ve canli konum icin tekrar kullanilabilir component olarak calismali.

## Mevcut Durum

- Base API v2 tarafina alinmis durumda: `https://rencarv2.halitkalayci.com/api/docs#/`
- Ehliyet onayi Swagger uzerinden yapilinca kullanici `CUSTOMER` oluyor ve harita aciliyor.
- Harita aciliyor, arac marker'lari ve secili arac karti gorunuyor.
- Rezervasyon, kiralama fotograf yukleme, kiralama baslatma, aktif kiralama ve Socket.IO canli konum akisi teslim icin kritik durumda.
- 4 yon fotograf ekrani var ama galeriden secilen fotograflar backend tarafinda kabul edilmeyebiliyor.
- Kiralama baslayinca haritada arac konumu, yol cizgisi, sure ve ucret canli gorunmeli.
- Odeme ekrani var; cuzdan/kart odemesi ve kart ekleme popup akisi teslim oncesi stabil hale getirilmeli.
- Profil gibi ikincil ekranlarda tiklanmayan alanlar var, ama ana teslim riski kiralama, harita, odeme ve API entegrasyonudur.

## Teslimde Gosterilecek Ana Senaryo

1. Login/register yapilir.
2. Ehliyet onayi gerekiyorsa Swagger admin ile onaylanir.
3. Harita acilir ve musait araclar listelenir.
4. Kullanici arac secer.
5. Rezervasyon olusturulur.
6. 4 yon arac fotografi galeriden secilir ve backend'e yuklenir.
7. 4 fotograf tamamlaninca kiralamayi baslat butonu aktif olur.
8. Kiralama baslayinca aktif yolculuk ekrani veya harita canli moda gecer.
9. Socket.IO `my-vehicle` eventi geldikce arac marker'i hareket eder ve rota cizgisi guncellenir.
10. Sure, mesafe ve anlik ucret gorunur.
11. Kiralama bitirilir.
12. Odeme ekrani acilir.
13. Cuzdan veya kart ile odeme tamamlanir; kayitli kart yoksa ayni ekranda popup ile kart eklenir.

## Ekip Branch Stratejisi

Main'e dogrudan calisilmayacak. Her is kucuk branch ile yapilip once build/test gecirilecek.

Ortak kural:
- Atalay data, API, DTO, repository, domain use case tarafinin sahibi olsun.
- Zeynep presentation, Compose ekranlari, MapLibre componentleri, UI metinleri ve demo akisi tarafinin sahibi olsun.
- Ayni dosyaya ayni anda girilmemeli. Zorunlu ortak dosyalar icin once kucuk bir "contract" commit'i atilmali.
- Her branch main'den acilmali ve kisa yasamali olmali.
- Merge sirasi: once API/domain, sonra UI baglama, en son polish.

## Branch 1: Atalay - Rental API ve Domain Akisi

Branch adi:

```text
feature/atalay-rental-api-contract
```

Sahip oldugu alanlar:
- `data/remote/RenCarApi.kt`
- `data/remote/dto/*Rental*`
- `data/remote/dto/*Reservation*`
- `data/repository/DefaultRentalRepository.kt`
- `data/repository/DefaultReservationRepository.kt`
- `domain/repository/RentalRepository.kt`
- `domain/repository/ReservationRepository.kt`
- `domain/usecase/*Rental*`
- `domain/usecase/*Reservation*`

Yapilacaklar:
- Swagger'daki en guncel endpointlerin Retrofit imzalarini kesinlestir.
- `POST /reservations` gercek endpoint ile calissin.
- `GET /reservations/active` aktif rezervasyonu donsun.
- `POST /rentals` rezervasyondan veya aractan yolculugu `PREPARING` durumunda olustursun.
- `POST /rentals/{id}/photos` 4 yon fotograf yuklemeyi dogru side degerleri ile gondersin.
- `GET /rentals/{id}/photos` 0/4, 1/4, 4/4 durumunu domain modeline cevirebilsin.
- `POST /rentals/{id}/start` sadece 4 fotograf tamamlaninca cagrilsin.
- `GET /rentals/active` aktif yolculuk state'ini donsun.
- `POST /rentals/{id}/finish` ve `POST /rentals/{id}/pay` odeme ekranina veri verecek sekilde calissin.
- Backend sadece JPG/PNG kabul ediyorsa galeriden gelen HEIC/WebP gibi dosyalar uygulamada JPEG'e cevrilsin veya kullaniciya acik mesaj verilsin.

Kabul kriterleri:
- `compileDebugKotlin` basarili.
- `testDebugUnitTest` basarili.
- Swagger ile ayni rental id uzerinden 4 fotograf yuklenebiliyor.
- 4 fotograf tamamlaninca repository `canStart=true` benzeri temiz domain state uretiyor.
- API hatalari UI'a ham JSON veya teknik mesaj olarak sizmiyor.

## Branch 2: Zeynep - Kiralama UI ve 4 Fotograf Deneyimi

Branch adi:

```text
feature/zeynep-rental-photo-ui-flow
```

Sahip oldugu alanlar:
- `presentation/ui/screens/rental/*`
- `presentation/ui/screens/reservation/*`
- `presentation/ui/components/*Photo*`
- `presentation/navigation/RenCarNavHost.kt`
- Sadece gerekli olursa ilgili MVI contract dosyalari

Yapilacaklar:
- "Rezerve et" ve "Kilidi ac" buton state'leri backend state ile baglansin.
- Buton pasifse neden pasif oldugu kullaniciya net gorunsun.
- 4 yon fotograf ekrani `0/4`, `1/4`, `4/4` sayacini backend cevabina gore guncellesin.
- Galeriden secilen fotograf yuklenince ilgili kutuda secildi/yuklendi durumu gorunsun.
- Yalnizca JPG/PNG kabul ediliyorsa hata mesaji dogal Turkce ile verilsin.
- "Kiralama Baslat" butonu 4 fotograf tamamlanmadan pasif kalsin; tamamlaninca aktif mavi butona donsun.
- Fotograf yukleme basarisiz olursa kullanici tekrar deneyebilsin, ekran donuk kalmasin.

UI metin onerileri:
- "Yalnizca JPG veya PNG dosyalari yuklenebilir."
- "Bu fotograf yuklenemedi. Lutfen JPG veya PNG bir gorsel sec."
- "4 fotograf tamamlandi. Kiralamayi baslatabilirsin."
- "Arac fotografi inceleniyor" yerine "Fotograflar kontrol ediliyor" gibi sade metin.

Kabul kriterleri:
- Galeriden 4 fotograf secilince ekranda 4/4 gorunur.
- Baslat butonu sadece 4/4 durumunda aktif olur.
- Hata mesajlari uzun cizgi veya yarim teknik cumle icermeden Turkce gorunur.
- Geri butonu modern, tema ile uyumlu ve tek elle kolay basilir olur.

## Branch 3: Atalay - Socket.IO ve Aktif Kiralama Domain Akisi

Branch adi:

```text
feature/atalay-socket-active-rental-domain
```

Sahip oldugu alanlar:
- `data/socket/*`
- `data/repository/*Location*`
- `domain/model/VehiclePoint.kt`
- `domain/repository/RideLocationRepository.kt`
- `domain/usecase/ObserveActiveVehicleLocationUseCase.kt`
- DI module dosyalari

Yapilacaklar:
- Socket.IO baglantisi `https://rencarv2.halitkalayci.com` uzerinden kurulacak.
- Namespace/event bilgisi Swagger ve hocanin gist'i ile dogrulanacak.
- CUSTOMER icin `my-vehicle` eventi dinlenecek.
- Gelen veri sade domain modeline cevrilecek:

```kotlin
data class VehiclePoint(
    val latitude: Double,
    val longitude: Double,
)
```

- Socket baglantisi aktif kiralama boyunca acik kalacak.
- Ekran kapaninca veya kiralama bitince socket tear down edilecek.
- Eski socket nesnesinden gelen gecikmis sinyaller state'i bozmamali.
- `while(true)` gibi sonsuz polling yazilmayacak; sadece socket event geldikce state guncellenecek.

Kabul kriterleri:
- Aktif kiralama yoksa socket sessiz kalir, UI cokmez.
- Aktif kiralama varsa ilk konum ve sonraki `my-vehicle` eventleri Flow olarak akar.
- Baglanti koparsa kullaniciya "Aracin GPS baglantisi tekrar kuruluyor" benzeri mesaj gosterilecek veri saglanir.

## Branch 4: Zeynep - MapLibre Canli Surus Deneyimi

Branch adi:

```text
feature/zeynep-map-live-rental-ui
```

Sahip oldugu alanlar:
- `presentation/ui/components/RenCarMap.kt`
- `presentation/ui/components/RenCarMapMarker.kt`
- `presentation/ui/screens/home/*`
- `presentation/ui/screens/activeRental/*`
- `presentation/navigation/RenCarNavHost.kt`

Yapilacaklar:
- Harita componenti sadece ana sayfaya bagli kalmayacak, aktif kiralama modunda da kullanilacak.
- Secili arac marker'i digerlerinden daha belirgin gorunsun.
- Canli konum geldikce arac marker'i hareket etsin.
- Gelen noktalarla haritada rota cizgisi olussun.
- Aktif kiralama panelinde sure, mesafe, anlik ucret ve durum gorunsun.
- "Canli veri bekleniyor" metni daha kullanici dostu hale getirilsin.

UI metin onerileri:
- "Aracin GPS baglantisi bekleniyor"
- "Arac konumu guncelleniyor"
- "Canli konum baglantisi yeniden kuruluyor"
- "Yolculuk basladi"
- "Yolculuk suresi"
- "Tahmini ucret"

Kabul kriterleri:
- `my-vehicle` eventi geldikce marker hareket eder.
- Rota cizgisi ekranda gorunur.
- Harita bos veya siyah kalmaz.
- Aktif kiralama bitince socket kapanir ve ekran odeme akisina gider.

## Branch 5: Atalay - Odeme, Cuzdan ve Kart API Son Kontrol

Branch adi:

```text
feature/atalay-payment-wallet-api-final
```

Sahip oldugu alanlar:
- `data/remote/dto/PaymentDto.kt`
- `data/remote/dto/WalletDto.kt`
- `data/remote/dto/IyzicoDto.kt`
- `data/repository/DefaultPaymentRepository.kt`
- `data/repository/DefaultWalletRepository.kt`
- `domain/repository/PaymentRepository.kt`
- `domain/repository/WalletRepository.kt`
- `domain/model/WalletInfo.kt`

Yapilacaklar:
- `GET /wallet` ve `POST /wallet/topup` son Swagger sozlesmesine gore calissin.
- `GET /cards`, `POST /cards`, `PATCH /cards/{id}/default`, `DELETE /cards/{id}` repository'de temiz olsun.
- `POST /rentals/{id}/pay` method olarak `WALLET`, `CARD`, `IYZICO` destekleyecek sekilde netlensin.
- Iyzico endpointleri simdilik zorunlu demo degilse izole kalsin; app'i kiralayip odemeyi engellemesin.
- Kart verisi gercek kart bilgisi gibi saklanmayacak; sadece backend'in istedigi gorsel/simule metadata gonderilecek.

Kabul kriterleri:
- Cuzdan ile odeme calisir.
- Kayitli kart ile odeme calisir.
- Kayitli kart yoksa UI yeni kart ekleme popup'ini acabilir.
- Payment API hatasi kullaniciya anlasilir mesajla doner.

## Branch 6: Zeynep - Odeme Ekrani ve Popup UX

Branch adi:

```text
feature/zeynep-payment-dialog-polish
```

Sahip oldugu alanlar:
- `presentation/ui/screens/payment/*`
- `presentation/ui/screens/wallet/*`
- `presentation/ui/components/*Card*`
- `presentation/ui/components/*Dialog*`

Yapilacaklar:
- Yolculuk bitince odeme ekranina gelindiginde kullanici baska sayfaya yollanmadan kart ekleyebilsin.
- Kayitli kart yoksa "Kart ekle" popup'i otomatik veya belirgin sekilde acilabilsin.
- Varsayilan kart secimi UI'da gorunsun.
- Cuzdan bakiyesi yetersizse kart veya cuzdan yukleme alternatifi sunulsun.
- Cuzdana bakiye yukleme alaninda kart secmeden girilen tutarin varsayilan karttan cekildigi acikca belli olsun.
- Odeme basarili olunca net sonuc ekrani gosterilsin.

UI metin onerileri:
- "Odeme icin kart ekle"
- "Bu kart varsayilan kartin olacak"
- "Cuzdan bakiyen yetersiz. Kart ile odeyebilir veya bakiye yukleyebilirsin."
- "Odeme tamamlandi"
- "Yolculuk ozeti"

Kabul kriterleri:
- Odeme ekraninda yeni sayfaya gitmeden kart popup'i acilir.
- Popup kapatilinca odeme ekrani state'i bozulmaz.
- Odeme basarili olunca gecmis kiralamalara dusen kayit gorulur.

## UI/UX Polish Gorevleri

Bu gorevler ana kiralama akisi calisir hale geldikten sonra yapilmali.

Zeynep:
- Ehliyet dogrulama ekraninda geri butonu mavi tema ile uyumlu, modern ve belirgin hale getirilecek.
- Ehliyet stepper'da on/arka yuklenince "Ehliyet" adimi tamamlanmis gibi gorunecek.
- Selfie yuklenince "Selfie" adimi tamamlanmis gibi gorunecek.
- Onay bekleniyorsa kullaniciya "Ehliyetiniz inceleniyor. Sonuc bildirildiginde devam edebilirsiniz." gibi sakin mesaj verilecek.
- Haritada "Canli veri bekleniyor" yerine "Aracin GPS baglantisi bekleniyor" yazilacak.
- Uyari metinlerinde teknik veya yarim cumle olmayacak.
- Uzun cizgiyle bolunmus mesajlar sade Turkce cumlelere cevrilecek.
- Profilde tiklanmayan alanlar ya calisir hale getirilecek ya da pasif gorunmeyecek.

Atalay:
- UI'in ihtiyac duydugu state'ler domain tarafinda tekil ve temiz modellerle saglanacak.
- Backend hata mesajlari tek bir error mapper ile kullanici dostu mesaja cevrilecek.
- Telefon normalizasyonu login, register ve verify-otp tarafinda tutarli kalacak.

## Swagger ile Kontrol Edilecek Endpointler

Teslimden once bu endpointler tek tek denenmeli:

- `POST /auth/login`
- `POST /auth/verify-otp`
- `GET /auth/me`
- `GET /vehicles`
- `GET /vehicles/{id}`
- `POST /reservations`
- `GET /reservations/active`
- `POST /rentals`
- `POST /rentals/{id}/photos`
- `GET /rentals/{id}/photos`
- `POST /rentals/{id}/start`
- `GET /rentals/active`
- `GET /rentals/{id}`
- `POST /rentals/{id}/finish`
- `POST /rentals/{id}/pay`
- `GET /wallet`
- `POST /wallet/topup`
- `GET /cards`
- `POST /cards`
- `PATCH /cards/{id}/default`
- `DELETE /cards/{id}`
- `PATCH /admin/licenses/{id}/approve`

## Demo Day Kontrol Listesi

- Temiz kurulumda login oluyor mu?
- `+90` telefon formati Swagger ve uygulamada ayni mi?
- Ehliyet onayi sonrasi yeni login ile token `CUSTOMER` geliyor mu?
- Harita bos kalmadan aciliyor mu?
- Arac secilince detay ve aksiyon butonlari anlasilir mi?
- Rezervasyon olusturulabiliyor mu?
- 4 fotograf yuklenebiliyor mu?
- Fotograf yukleme hatasinda kullanici ne yapacagini anliyor mu?
- Kiralama baslatilabiliyor mu?
- Aktif kiralamada sure, mesafe ve ucret gorunuyor mu?
- Socket.IO `my-vehicle` geldikce harita marker'i hareket ediyor mu?
- Rota cizgisi gorunuyor mu?
- Kiralama bitince odeme ekranina gidiyor mu?
- Kayitli kart yokken popup ile kart eklenebiliyor mu?
- Cuzdanla odeme calisiyor mu?
- Kartla odeme calisiyor mu?
- Odeme sonrasi kiralama gecmiste gorunuyor mu?

## README Icin Alinacak Ekran Goruntuleri

- Onboarding veya login ekrani.
- Harita ve arac marker'lari.
- Arac detay veya rezervasyon ekrani.
- 4 yon fotograf yukleme ekrani.
- Aktif kiralama ve canli rota ekrani.
- Odeme ekrani.
- Kart ekleme popup'i.
- Kiralama gecmisi.
- Swagger v2 dokumani ve Socket.IO aciklamasi.

## Merge Sirasi

1. `feature/atalay-rental-api-contract`
2. `feature/zeynep-rental-photo-ui-flow`
3. `feature/atalay-socket-active-rental-domain`
4. `feature/zeynep-map-live-rental-ui`
5. `feature/atalay-payment-wallet-api-final`
6. `feature/zeynep-payment-dialog-polish`
7. UI polish ve README ekran goruntuleri

Her merge oncesi:

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
```

Her merge sonrasi emulator smoke test:

```text
Login -> Harita -> Arac sec -> Rezervasyon -> 4 fotograf -> Kiralamayi baslat -> Canli konum -> Bitir -> Ode
```

## Oncelik Sirasi

1. Kiralama API akisi ve 4 fotograf yukleme sorunu.
2. Kiralamayi baslat ve aktif kiralama state'i.
3. Socket.IO `my-vehicle` harita baglantisi.
4. Odeme ekrani, cuzdan ve kart popup'i.
5. UI metinleri, geri butonlari, profil gibi ikincil polish alanlari.

Bu siraya uyarsak uygulamanin ana anlami olan "arac kirala, canli takip et, ode" akisi once kurtarilir. Profil gibi detaylar sonra iyilestirilir.
