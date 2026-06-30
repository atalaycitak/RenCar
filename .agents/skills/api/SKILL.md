---
name: api
description: RenCar projesi için Retrofit kullanarak REST API tanımlama ve tüketme kuralları.
---

# API Guidelines

## Base URL ve Dokümantasyon
- **API Base URL:** `https://rencar.halitkalayci.com/`
- **Swagger Dokümantasyonu:** `https://rencar.halitkalayci.com/api/docs`

## Kurallar ve Standartlar
- Ağ işlemleri (Networking) için **Retrofit** ve **OkHttp** kullanılmalıdır.
- API endpoint'leri Kotlin interface'leri içerisinde, Coroutines desteğiyle `suspend` fonksiyonlar olarak tanımlanmalıdır.
- Başarılı ve hatalı API yanıtlarını güvenli bir şekilde işlemek için `NetworkResponse<T>` veya benzeri jenerik (generic) sarmalayıcı sınıflar (sealed classes) oluşturulmalı ve kullanılmalıdır.
- Retrofit instance'ı ve servis interface'leri bağımlılık enjeksiyonu (DI) aracı olan **Koin** üzerinden projeye sağlanmalıdır.
- Gelen ve giden JSON verilerinin parse edilmesi için `kotlinx.serialization` veya `Gson` (tercihen `kotlinx.serialization`) kullanılmalıdır.
