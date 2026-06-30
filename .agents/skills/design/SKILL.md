---
name: design
description: RenCar projesi için Jetpack Compose UI geliştirme ve MapLibre entegrasyonu kuralları.
---

# Design System Guidelines

## UI Framework (Jetpack Compose)
- Kullanıcı arayüzü yalnızca **Jetpack Compose** kullanılarak geliştirilmelidir. Çok zorunlu bir Legacy kütüphane desteği gerekmedikçe XML kullanılmamalıdır.
- Component'ler (bileşenler) olabildiğince "stateless" (durumsuz) olarak tasarlanmalı ve yeniden kullanılabilir (reusable) hale getirilmelidir.
- Durum (State) üst seviyede (Ekran composable'ında) tutulmalı (state hoisting) ve alt component'lere argüman olarak geçilmelidir.

## Tasarım Sistemi ve Tema
- Uygulamanın marka renkleri, fontları (Typography) ve şekilleri (Shapes) Compose `MaterialTheme` üzerinden merkezi bir `Theme.kt` dosyasında yapılandırılmalıdır.
- Dark Mode ve Light Mode için renk paletleri ayrı ayrı tanımlanmalı ve desteklenmelidir.
- Verilen PDF tasarımındaki animasyonlu ve modern görünümler (gölgeler, geçişler) sadakatle uygulanmalıdır.

## Harita (MapLibre)
- Google Maps yerine **MapLibre** kullanılacaktır.
- Harita bileşenleri Compose ile uyumlu olacak şekilde, gerekiyorsa `AndroidView` sarmalayıcısı (wrapper) ile veya MapLibre'nin güncel Compose desteği ile entegre edilmelidir.
- Harita üzerindeki markörler (araç konumları) dinamik olmalı ve state güncellendikçe reaktif olarak yer değiştirmelidir.

## Yardımcı Kütüphaneler
- Görsel yükleme işlemleri için Compose ile mükemmel uyumlu olan **Coil** kullanılacaktır.
