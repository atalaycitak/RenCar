# RenCar Projesi Geliştirme Kuralları

Bu doküman RenCar Android (Kotlin) projesinin geliştirme süreçlerinde uyulması gereken temel standartları içerir.

## Teknolojiler ve Mimari
- **Mimari:** Clean Architecture ve MVI (Model-View-Intent).
- **UI Framework:** Tamamen Jetpack Compose.
- **Dependency Injection:** Koin.
- **Network / API:** Retrofit + OkHttp. 
  - **Base URL:** `https://rencar.halitkalayci.com/`
  - **Docs:** `https://rencar.halitkalayci.com/api/docs`
- **Harita (Map):** MapLibre.
- **Ödeme:** İlerleyen aşamalarda Iyzico kullanılacak.
- **Asenkron Programlama:** Kotlin Coroutines & Flow (StateFlow, SharedFlow).

## Temel Geliştirme Prensipleri
- Kodlar mümkün olduğunca modüler tutulacak, bileşenler tekrar kullanılabilir şekilde tasarlanacaktır.
- MVI state'leri mutable (değiştirilebilir) olmamalı, `copy` metoduyla yeni kopyalar üretilerek state güncellenmelidir.
- Ağ ve veritabanı işlemleri ana (Main) thread üzerinde yapılmamalıdır.
