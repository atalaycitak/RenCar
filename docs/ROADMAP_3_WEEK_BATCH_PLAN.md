# RenCar 3 Haftalik Batch Plani

## Hedef

RenCar Android MVP'sini 3 hafta icinde calisir, demo edilebilir ve gelistirmeye devam edilebilir bir seviyeye getirmek.

Plan baslangici: 1 Temmuz 2026  
Plan bitisi: 21 Temmuz 2026

## Kaynaklar

- Repo: `atalaycitak/RenCar`
- Tasarim girdisi: arac kiralama mobil tasarimi PDF'i
- Harita kaynagi: `https://maplibre.org/`
- API dokumani: `https://rencar.halitkalayci.com/api/docs`
- OpenAPI JSON: `https://rencar.halitkalayci.com/api/docs-json`
- Katki email'i: `csezeze@gmail.com`

## Mimari Omurga

Proje Kotlin, Jetpack Compose ve MVI ile ilerlemeli. MVP icin tek `app` modulu icinde feature-first paketleme yeterlidir. Her feature `Route`, `Screen`, `Contract`, `ViewModel` ve gerekirse `components` klasoru ile ayni sekilde kurulmalidir. Harita entegrasyonu MapLibre ile, backend entegrasyonu mevcut OpenAPI semasiyla yapilmalidir.

## Batch Ozeti

| Batch | Tarih | Odak | Cikti |
| --- | --- | --- | --- |
| Batch 01 | 1-3 Temmuz | Proje temeli, MVI iskeleti, tasarim sistemi, onboarding, API auth baslangici | Calisan app shell |
| Batch 02 | 4-7 Temmuz | Ehliyet dogrulama, izinler, MapLibre harita shell | Dogrulama ve home akisi |
| Batch 03 | 8-10 Temmuz | Arac liste/detay, rezervasyon, teslim checklist | Rezervasyon akisi |
| Batch 04 | 11-14 Temmuz | Aktif kiralama, yolculuk bitisi, odeme ve cuzdan | Kiralama yasam dongusu |
| Batch 05 | 15-17 Temmuz | Kiralamalar, profil, ayarlar, dark theme tamamlama | Ana tablar tamam |
| Batch 06 | 18-21 Temmuz | QA, test, hata kapatma, release hazirligi | Demo/release adayi |

## Kritik Yol

1. Ilk iki gunde proje iskeleti ve navigation ayaga kalkmali.
2. 7 Temmuz sonunda kullanici onboardingden harita ekranina ulasabilmeli.
3. 10 Temmuz sonunda bir arac secilip rezervasyon tamamlanabilmeli.
4. 14 Temmuz sonunda aktif kiralama baslayip odeme ozetine dusmeli.
5. 17 Temmuz sonunda tab navigasyonu ve profil/kiralamalar tamamlanmali.
6. 21 Temmuz sonunda testler, polish ve release notlari hazir olmali.

## Batch Dosyalari

- `docs/tasks/BATCH_01_FOUNDATION_AUTH.md`
- `docs/tasks/BATCH_02_VERIFICATION_MAP.md`
- `docs/tasks/BATCH_03_RESERVATION_DELIVERY.md`
- `docs/tasks/BATCH_04_ACTIVE_RENTAL_PAYMENTS.md`
- `docs/tasks/BATCH_05_HISTORY_PROFILE_POLISH.md`
- `docs/tasks/BATCH_06_RELEASE_QA.md`

## Gunluk Calisma Ritmi

- Gun basi: onceki gun kalanlar ve bugun bitecek tasklar netlestirilir.
- Gun ortasi: UI ve state akislarinda blokaj var mi kontrol edilir.
- Gun sonu: calisan ekranlar emulator uzerinde kontrol edilir, kisa not dusulur.

## Done Kriterleri

- App build aliyor.
- Ana akista crash yok.
- ViewModel state testleri kritik feature'larda mevcut.
- Dark/light tema goze batan fark olmadan calisiyor.
- Navigation geri tusu davranislari kontrol edilmis.
- Fake data ile demo akisi bastan sona tamamlanabiliyor.
- Commit ve PR metinlerinde gelistirme araci markasi veya otomatik uretim ibaresi yok.

## Riskler

| Risk | Etki | Onlem |
| --- | --- | --- |
| Harita tile/style bilgisi gecikir | Harita gorseli bloklanir | MapLibre adapter hazirlanir, gecici style veya fake map ile devam edilir |
| API auth veya token akisi degisir | Login ve korumali istekler etkilenir | OpenAPI JSON tekrar kontrol edilir, Retrofit contract guncellenir |
| Tasarim kapsam disina tasar | 3 haftalik plan sarkar | MVP disi animasyonlar ve edge case'ler backlog'a alinir |
| Odeme entegrasyonu belirsiz | Cuzdan akisi bloklanir | UI ve domain model fake payment status ile tamamlanir |

## MVP Disi Backlog

- Iyzico odeme saglayici entegrasyonu
- Telefon/OTP auth endpointleri
- Canli arac telemetri akisi
- Push notification
- Kampanya ve kupon sistemi
- Kurumsal hesaplar
- Admin panel
- Destek chat akisi
