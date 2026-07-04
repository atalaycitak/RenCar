# RenCar Projesi Geliştirme Kuralları (GOVERNANCE)

> Bu repository RenCar adında bir araç kiralama uygulamasının kaynak kodlarını içermektedir. Bu `AGENTS.md` dosyası, bu projede çalışan insan/AI tüm geliştiricilerin KESİNLİKLE uyması ZORUNLU olduğu genel kuralları içerir. Kuralların ihlali kabul edilemez.

---

## 1. TEKNOLOJİ YIĞINI

- **Mobil (UI):** Android Jetpack Compose (KOTLIN)
- **Mimari:** Clean Architecture + MVI (Model-View-Intent)
- **Dependency Injection:** Koin (Hilt KESİNLİKLE YASAKTIR)
- **Network / API:** Retrofit + OkHttp
  - **Base URL:** `https://rencar.halitkalayci.com/`
  - **Docs:** `https://rencar.halitkalayci.com/api/docs`
- **Harita:** MapLibre
- **Kamera:** Google CameraX
- **Asenkron Programlama:** Kotlin Coroutines & Flow (StateFlow, SharedFlow)

## 2. GENEL ÇALIŞMA PRENSİPLERİ

### 2.1. TEK SEFERDE DOSYA LİMİTİ
Hangi işlem olursa olsun, tek seferde maksimum (birbiriyle alakalı) 5 dosya üzerinde işlem yapılabilir. Geliştirme süreci birbiriyle mantıksal olarak bağlantılı, küçük ve doğrulanabilir "batch"lere bölünmek zorundadır. Aksi talep edilirse DUR ve EK ONAY İSTE.

### 2.2. UYDURMAK YASAK (NO INVENTING)
Herhangi bir operasyonda bilgi ya da referans eksikliği (API şeması, tasarım detayı vb.) yaşanıyorsa, eksik/hatalı bilgiyi "uydurmak" kesinlikle yasaktır. Böyle bir durumda operasyon DERHAL DURDURULMALI ve kullanıcıdan ek bilgi talep edilmelidir.

### 2.3. ÖNCE PLANLA, SONRA KODLA
Herhangi bir kaynak kodu üretilmeden önce aşağıdaki adımlar zorunludur:
- Modifiye edilecek, silinecek veya eklenecek dosyaların tam bir dökümünü (`implementation_plan.md` formatında) sunmak.
- Yeni bağımlılıklar eklenecekse sürüm ve nedenlerini listelemek.
- Kullanıcıdan KESİN ONAY almadan asla uygulamaya (implementation) geçmemek.

### 2.4. MİMARİ KURALLAR
Sunum (Presentation) katmanı SADECE **MVI** ile yazılır. Mimari prensiplerden sapmak kesinlikle yasaktır. 
Bir MVI ekranı geliştirilirken aşağıdaki dokümanlar BAĞLAYICI REFERANSTIR ve okunmaları zorunludur:
- `docs/architecture/mvi-overview.md` — Genel prensipler, veri akışı ve katman/paket yapısı.
- `docs/architecture/mvi-contracts.md` — State + Intent + Effect kural seti.
- `docs/architecture/mvi-viewmodel-rules.md` — ViewModel asenkron kısıtlamaları ve Koin entegrasyonu.

## 3. ÇIKTI (OUTPUT) FORMATI
Geliştiricilerin ve AI ajanlarının her aksiyon sonrası aşağıdaki çıktı formatına uyması ZORUNLUDUR:
- **Resmi bir dil kullanmak zorundasın. Emoji veya laubali ifadeler kullanmak KESİNLİKLE YASAKTIR.**
- Gerçekleştirilen adımların net ve profesyonel bir özetini sun.
- Değiştirilen dosyaların listesini (`walkthrough.md` veya mesaj içinde) raporla.
- Her implementasyon sonrası kodun derlendiğinden (`./gradlew compileDebugKotlin`) emin ol ve sonucu kanıtla.

## 4. AI-NATIVE GELİŞTİRME PRENSİPLERİ

### 4.1. SKILL (YETENEK) KULLANIMI
Ajanlar, karmaşık işlemler için `.agents/skills/` klasöründeki yetenek yönergelerini okumakla yükümlüdür. Ajan, göreve başlamadan önce bağlam ile ilgili `SKILL.md` dosyasını okumalı ve talimatları harfiyen yerine getirmelidir.

### 4.2. DOKÜMANTASYON EŞZAMANLILIĞI
Projeye yeni bir özellik, mimari yapı veya API eklendiğinde, ilgili dokümantasyon senkronize olarak güncellenmek zorundadır. Kod ve dokümantasyon asla birbirinden kopuk olamaz.
