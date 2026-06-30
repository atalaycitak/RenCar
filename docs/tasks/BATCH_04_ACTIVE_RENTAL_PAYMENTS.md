# Batch 04 - Aktif Kiralama, Odeme ve Cuzdan

Tarih: 11-14 Temmuz 2026

## Hedef

Aktif kiralama ekranini, yolculuk tamamlandi ozetini, odeme durumunu ve cuzdan ekranlarini MVP seviyesinde tamamlamak.

## Tasklar

- [ ] RentalSession domain modelini tamamla.
- [ ] Aktif kiralama ekranini uygula: sure, mesafe, anlik tutar, rota placeholder.
- [ ] Kiralama bitirme aksiyonunu state ve effect olarak modelle.
- [ ] `POST /rentals/{id}/return` ile kiralama iade akisini bagla.
- [ ] `GET /rentals/{id}` ile kiralama detayini cek.
- [ ] Yolculuk tamamlandi ekranini uygula.
- [ ] Ucret kalemlerini modelle: sure, mesafe, indirim, toplam.
- [ ] PaymentMethod domain modelini ekle.
- [ ] Odeme ozeti ve secili kart UI'ini uygula.
- [ ] Cuzdan ekranini uygula.
- [ ] Kart listesi ve islem listesi fake veriyle calissin.
- [ ] Bakiye yukleme UI state'ini ekle.
- [ ] Odeme ve cuzdan ViewModel testlerini yaz.
- [ ] Aktif kiralama bitince kiralamalar gecmisine kayit dusmesini fake repository'de sagla.

## Kabul Kriterleri

- Kullanici teslim checklist sonrasi aktif kiralama ekranina ulasir.
- Kiralama bitirildiginde odeme ozeti acilir.
- Odeme fake olarak basarili tamamlanir.
- Cuzdan tab'i bakiye, kartlar ve islemleri gosterir.
- Ana demo akisi bastan odeme sonuna kadar calisir.

## Teknik Not

OpenAPI semasinda odeme ve cuzdan endpointleri bulunmuyor. Bu batch'te odeme/cuzdan UI'i fake repository ile korunacak; gercek entegrasyon daha sonra repository implementation degisimiyle eklenecektir.
