---
name: architecture
description: RenCar projesinde kullanılacak olan MVI (Model-View-Intent) ve Clean Architecture kuralları.
---

# Architecture Guidelines

## Clean Architecture Katmanları
Proje, bağımlılıkları dıştan içe doğru akacak şekilde katmanlandırılmalıdır:
1. **Data Katmanı:** API servisleri, Retrofit implementasyonları, Local veritabanı (DataStore/Room), Repository implementasyonları ve DTO (Data Transfer Object) modellerini içerir.
2. **Domain Katmanı:** UseCase'leri (Interactor'lar), temel iş kurallarını (business logic), saf domain modellerini ve Repository interface'lerini içerir. Hiçbir Android kütüphanesine bağımlı olmamalıdır.
3. **Presentation Katmanı:** ViewModel sınıflarını (MVI yöneticileri), Jetpack Compose UI ekranlarını, durum (State) ve olay (Intent) sınıflarını içerir.

## MVI (Model-View-Intent) Kuralları
Her ekranın Presentation katmanında aşağıdakiler bulunmalıdır:
- **State (Durum):** Ekranın anlık verisini tutan tek bir `data class`. (Örn: `HomeState`).
- **Intent / Event (Eylem):** Kullanıcının veya sistemin tetiklediği eylemleri temsil eden `sealed class`. (Örn: `HomeIntent.OnCarSelected`).
- **Effect / SideEffect:** Ekrandaki tek seferlik olayları (örn: Navigasyon, Toast mesajı gösterme) yöneten `sealed class`.
- **ViewModel:** Intent'leri dinleyip (processIntent) gerekli UseCase'leri çağırarak, dönen sonuca göre State'i (`MutableStateFlow`) güncelleyen ve SideEffect'leri (`MutableSharedFlow`) fırlatan bileşendir.
