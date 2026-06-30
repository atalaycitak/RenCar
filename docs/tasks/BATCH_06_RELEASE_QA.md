# Batch 06 - QA, Test ve Release Hazirligi

Tarih: 18-21 Temmuz 2026

## Hedef

MVP'yi demo/release adayi seviyesine getirmek, kritik hatalari kapatmak, testleri tamamlamak ve teslim dokumanlarini hazirlamak.

## Tasklar

- [ ] Tum ana demo akisini emulator uzerinde bastan sona test et.
- [ ] Navigation geri tusu ve tab gecislerini test et.
- [ ] Form validation edge case'lerini kontrol et.
- [ ] Permission red/fallback state'lerini kontrol et.
- [ ] Loading, error ve empty state'leri elle test et.
- [ ] ViewModel unit test kapsaminda kritik ekranlari tamamla.
- [ ] En az bir Compose UI smoke test ekle.
- [ ] Gradle build ve test komutlarini temiz calistir.
- [ ] OpenAPI endpointleriyle smoke test yap: health, login, vehicles, rentals.
- [ ] Gereksiz TODO ve debug loglari temizle.
- [ ] README icin kurulum ve calistirma notlarini hazirla.
- [ ] MVP release notlarini hazirla.
- [ ] Commit mesajlarini ve PR metnini marka/otomatik uretim ibaresi acisindan kontrol et.
- [ ] Son demo videosu veya ekran goruntusu listesi cikar.

## Kabul Kriterleri

- Build basarili.
- Unit testler basarili.
- Ana demo akisi crash olmadan tamamlanir.
- README, mimari not ve release notu gunceldir.
- Katki email'i `csezeze@gmail.com` olarak ayarlanmistir.
- Commit/PR metinlerinde gelistirme araci markasi veya otomatik uretim ibaresi yoktur.

## Release Adayi Demo Akisi

`Onboarding -> Login/Register -> Ehliyet -> Harita -> Arac Detay -> Rezervasyon -> Teslim -> Aktif Kiralama -> Yolculuk Tamamlandi -> Odeme -> Cuzdan -> Kiralamalar -> Profil`
