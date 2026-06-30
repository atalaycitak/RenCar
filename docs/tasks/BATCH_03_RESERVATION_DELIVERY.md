# Batch 03 - Arac Secimi, Rezervasyon ve Teslim

Tarih: 8-10 Temmuz 2026

## Hedef

Harita uzerinden arac secimini, arac detayini, rezervasyon onayini ve araca teslim checklist akisini tamamlamak.

## Tasklar

- [ ] Vehicle domain modelini tamamla: id, marka, model, plaka, menzil, fiyat, lokasyon, durum.
- [ ] `GET /vehicles/{id}` ile arac detayini cek.
- [ ] Haritadaki arac kartini tasarima uygun uygula.
- [ ] Arac detay ekranini uygula.
- [ ] Rezervasyon onay ekranini uygula.
- [ ] Rezervasyon sure, fiyat ve ek ucret hesaplarini fake use case ile modelle.
- [ ] Rezervasyon repository arayuzunu real ve fake implementation ile yaz.
- [ ] `POST /rentals` request'ini `vehicleId` ve `endDate` ile bagla.
- [ ] Rezervasyon basarili state'inden teslim akisine navigation ekle.
- [ ] Teslim checklist ekranini uygula.
- [ ] Arac durumu, fotograf ve anahtar/kapilar gibi teslim adimlarini state'e bagla.
- [ ] Rezervasyon ve teslim ViewModel testlerini ekle.

## Kabul Kriterleri

- Kullanici haritadan arac secer.
- Arac detayindan rezervasyon onayina gider.
- Rezervasyon tamamlaninca teslim checklist akisi acilir.
- Checklist tamamlanmadan aktif kiralama baslatilmaz.
- Fiyat hesaplari tek bir use case icinden gelir.

## Demo Akisi

`Onboarding -> Login/Register -> Ehliyet -> Harita -> Arac Detay -> Rezervasyon -> Teslim Checklist`
