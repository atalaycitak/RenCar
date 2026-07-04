---
name: maplibre-integration
description: Guidelines and rules for integrating MapLibre Maps into the RenCar Compose application.
---
# MapLibre Integration

Sen RenCar projesinde Harita (Map) tabanlı bir işlem yapması istenen uzmansın. Projede harita altyapısı olarak Google Maps YERİNE, **MapLibre** kullanılmaktadır.

## 1. Harita Bileşeninin Kullanımı
- Mevcut `RenCarMap` bileşeni (`app/src/main/java/com/example/rencar_pair/presentation/ui/components/RenCarMap.kt`) her zaman referans alınmalıdır.
- Harita üzerinde bir Marker (İşaretçi) gösterilecekse `RenCarMapMarker` bileşeni kullanılmalıdır.

## 2. Stil ve Harita URL'leri
- MapLibre yapılandırmasında kullanılacak harita stil URL'i projede tanımlı olmalıdır. Sabit (hardcoded) veya güvenliği olmayan demo URL'ler geçici olarak kabul edilebilir ancak Production için değiştirilmesi gerektiği unutulmamalıdır.

## 3. Konum ve İzinler
- Harita ile ilgili herhangi bir entegrasyon yapılmadan önce uygulamanın `ACCESS_FINE_LOCATION` izinlerinin alındığından emin olunmalıdır. Kullanıcı konumu `FusedLocationProviderClient` üzerinden asenkron olarak (Flow/Coroutine ile) takip edilmelidir. Ana thread bloklanmamalıdır.
