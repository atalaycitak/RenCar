# Batch 01 - Foundation ve Auth Baslangici

Tarih: 1-3 Temmuz 2026

## Hedef

Android projesini calisir hale getirmek, MVI iskeletini standartlastirmak, temel tasarim sistemini kurmak ve onboarding/giris akisini baslatmak.

## Tasklar

- [ ] Android Studio projesini Kotlin + Compose + Material 3 ile olustur.
- [ ] Paket adini ve app id'yi netlestir: `com.rencar.app`.
- [ ] Gradle versiyon katalogu kur.
- [ ] Koin, Navigation Compose, Coroutines, Coil ve test kutuphanelerini ekle.
- [ ] Retrofit, OkHttp ve kotlinx.serialization kararini uygulamaya ekle.
- [ ] `core`, `data`, `domain`, `feature` paketlerini olustur.
- [ ] `RenCarTheme` light/dark tema temelini kur.
- [ ] Renk, typography ve spacing tokenlarini ekle.
- [ ] Ortak UI bilesenlerini yaz: primary button, text field, top bar, loading, error.
- [ ] MVI contract ornegini bir feature uzerinde sabitle.
- [ ] Navigation root ve route isimlerini olustur.
- [ ] Onboarding light/dark ekranlarini uygula.
- [ ] Email/parola girisi ekranini uygula.
- [ ] Kayit ekranini uygula: email, parola, ad soyad, telefon.
- [ ] Auth API servislerini ekle: register, login, refresh, logout, me.
- [ ] Token saklama icin DataStore altyapisini kur.
- [ ] Auth fake ve real repository iskeletlerini yaz.
- [ ] Auth ViewModel state testlerini ekle.
- [ ] Git author email'inin `csezeze@gmail.com` oldugunu kontrol et.

## Kabul Kriterleri

- App emulator uzerinde acilir.
- Onboarding ekranindan telefon girisine gecilir.
- Email/parola validation calisir.
- Register ve login akislarinda API contract'i OpenAPI semasiyla uyumludur.
- Basarili auth akisi ehliyet dogrulama veya harita ekranina navigation effect uretir.
- Tema light/dark modda kirilmaz.
- Commit metinlerinde gelistirme araci markasi veya otomatik uretim ibaresi yoktur.

## Notlar

Bu batch sonunda gorsel kalite mukemmel olmak zorunda degil; onemli olan mimari iskeletin dogru oturmasi ve sonraki feature'larin ayni kalipla hizli yazilabilmesidir.
