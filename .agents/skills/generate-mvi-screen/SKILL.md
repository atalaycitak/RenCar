---
name: generate-mvi-screen
description: Generates a complete Compose MVI screen for RenCar following strict Clean Architecture and Koin rules.
---
# Generate MVI Screen

Sen RenCar için yeni bir ekran tasarlayacak olan Kıdemli Compose/MVI Mimarı ajansın. Bu yetenek (skill), projede MVI mimarisine sadık kalarak sıfırdan bir UI/Ekran oluşturmak istediğinde tetiklenir.

## 1. Dizin ve Dosya Yapısı
Yeni eklenecek her özellik (feature) Presentation katmanı altında kendi klasöründe olmalıdır:
`app/src/main/java/com/example/rencar_pair/presentation/ui/screens/<feature_name>/`

Bu klasör içinde şu dosyaların oluşturulması ZORUNLUDUR:
- `<Feature>Contract.kt` (MviState, MviIntent, MviEffect arayüzlerini uygulamalıdır).
- `<Feature>ViewModel.kt` (BaseMviViewModel'den türemeli ve iş mantığını yönetmelidir).
- `<Feature>Screen.kt` (StateFlow'u okuyan, Intent fırlatan Compose arayüzü).

## 2. Kural Setleri (Zorunlu Okuma)
Ekranı üretmeden önce aşağıdaki dokümanların içeriğini okuman ZORUNLUDUR:
1. `docs/architecture/mvi-contracts.md`
2. `docs/architecture/mvi-viewmodel-rules.md`

## 3. Mimari ve DI Standartları
- **Dependency Injection:** Hilt KESİNLİKLE kullanılmayacaktır. ViewModel'ler Koin modüllerine (`di/AppModule.kt`) `viewModelOf` kullanılarak kaydedilmelidir. Compose ekranı içinde `val viewModel: FeatureViewModel = koinViewModel()` ile çağrılmalıdır.
- **Kullanıcı Deneyimi:** Tasarımlar profesyonel standartlarda (Premium) olmalı, sabit (hardcoded) metinler yerine string resource'ları veya dinamik veriler tercih edilmeli, MaterialTheme standartları uygulanmalıdır.

## 4. Onay Aşaması
Kodları doğrudan fiziksel dosyalara yazmadan önce `implementation_plan.md` üzerinden planlama yapılmalı ve kullanıcıdan onay beklenmelidir.
