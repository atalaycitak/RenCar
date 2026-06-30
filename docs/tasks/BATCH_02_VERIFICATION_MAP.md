# Batch 02 - Ehliyet Dogrulama ve MapLibre Harita Shell

Tarih: 4-7 Temmuz 2026

## Hedef

Ehliyet dogrulama ekranlarini kurmak, izinleri modellemek ve MapLibre tabanli harita ana ekranini API/fake veriyle calisir hale getirmek.

## Tasklar

- [x] OpenAPI semasinda OTP endpoint'i olmadigi icin OTP ekranini backlog olarak isaretle.
- [x] Ehliyet dogrulama giris ekranini uygula.
- [x] Ehliyet foto/on-yuz yukleme UI state'lerini ekle.
- [x] `POST /license/upload` icin multipart veya base64 request formatini dogrula.
- [x] `GET /license/status` ile dogrulama durumunu cek.
- [x] Dogrulama basarili/beklemede/hata durumlarini modelle.
- [x] Kamera ve konum izinleri icin permission helper yaz.
- [x] Harita ekraninin route ve state contract'ini olustur.
- [x] MapLibre dependency ve temel map wrapper'i ekle.
- [x] Fake arac lokasyonlari ve fiyat bilgilerini domain model'e ekle.
- [x] `GET /vehicles` response'unu map marker ve yakin arac paneline bagla.
- [x] Harita shell'i kur: arama kutusu, marker, yakin arac paneli.
- [x] Bottom navigation temelini ekle.
- [x] Dogrulama tamamlaninca harita ekranina gecis sagla.

## Kabul Kriterleri

- Kullanici onboarding, auth ve ehliyet dogrulama akisindan haritaya ulasir.
- Harita ekrani API veya fake arac verilerini listeler.
- Izin verilmediginde anlamli fallback state gosterilir.
- ViewModel'lar navigation ve hata effect'lerini ayri uretir.

## Risk Notu

MapLibre style/tile bilgisi hazir degilse bu batch fake map UI ile kapatilabilir. Style bilgisi geldigi anda adapter katmani degistirilerek canli haritaya gecilmelidir.
