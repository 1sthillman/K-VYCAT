# 🚀 Windows'ta APK Oluşturma - Süper Basit!

## ⚡ Hızlı Başlangıç (3 Adım)

### 1️⃣ Gereksinimleri Kontrol Et

```batch
# Java kontrolü
java -version
```

**Java 17 gerekli!** Yoksa indir: https://adoptium.net/

### 2️⃣ APK Oluştur

```batch
# MXWPrinter klasörüne git
cd C:\print\TiMini-Print\MXWPrinter

# Debug APK oluştur (test için)
BUILD_WINDOWS.bat
```

### 3️⃣ Telefona Yükle

**Yöntem A: USB ile**
```batch
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**Yöntem B: Dosya ile**
- APK'yı telefona kopyala
- Dosya yöneticisinden aç
- Yükle

## 📱 Detaylı Adımlar

### Gereksinimler

✅ **Java Development Kit (JDK) 17**
- İndir: https://adoptium.net/
- Kurulum sonrası terminali yeniden başlat

✅ **Android Studio** (opsiyonel ama önerilen)
- İndir: https://developer.android.com/studio
- Android SDK otomatik yüklenir

✅ **İnternet Bağlantısı**
- İlk build'de bağımlılıklar indirilir

### Debug APK (Test için)

```batch
# 1. Klasöre git
cd C:\print\TiMini-Print\MXWPrinter

# 2. Build script'i çalıştır
BUILD_WINDOWS.bat

# 3. Bekle (2-5 dakika)
# İlk build daha uzun sürer

# 4. APK hazır!
# Konum: app\build\outputs\apk\debug\app-debug.apk
```

### Release APK (Yayın için)

```batch
# 1. Release script'i çalıştır
BUILD_RELEASE.bat

# 2. Keystore oluştur (ilk seferde)
# Şifre ve bilgileri GÜVENLİ sakla!

# 3. Bekle (3-7 dakika)

# 4. İmzalı APK hazır!
# Konum: app\build\outputs\apk\release\MXWPrinter-release.apk
```

## 🔧 Sorun Giderme

### "Java bulunamadı"

```batch
# Java yükle
# https://adoptium.net/ → JDK 17

# Kurulum sonrası kontrol et
java -version
```

### "Gradle sync failed"

**Çözüm 1: Android Studio ile**
1. Android Studio'yu aç
2. MXWPrinter klasörünü aç
3. "Sync Project with Gradle Files" tıkla
4. Bekle
5. BUILD_WINDOWS.bat'ı tekrar çalıştır

**Çözüm 2: Manuel**
```batch
# Gradle wrapper'ı güncelle
gradlew.bat wrapper --gradle-version=8.9

# Tekrar dene
BUILD_WINDOWS.bat
```

### "Build failed"

```batch
# Temizle ve tekrar dene
gradlew.bat clean
BUILD_WINDOWS.bat
```

### "APK bulunamadı"

```batch
# Manuel build
gradlew.bat assembleDebug

# APK konumunu kontrol et
dir app\build\outputs\apk\debug\
```

## 📦 APK Boyutu

- **Debug APK**: ~8-12 MB
- **Release APK**: ~6-8 MB (optimize edilmiş)

## 🎯 Build Tipleri

### Debug APK
- ✅ Test için ideal
- ✅ Hızlı build
- ✅ USB debugging
- ❌ Play Store'a yüklenemez

### Release APK
- ✅ Play Store'a yüklenebilir
- ✅ Optimize edilmiş
- ✅ İmzalı
- ⏱️ Daha uzun build

## 📱 Telefona Yükleme

### USB ile (ADB)

```batch
# 1. USB Debugging'i aç
# Ayarlar → Geliştirici Seçenekleri → USB Debugging

# 2. Telefonu bağla

# 3. ADB kontrolü
adb devices

# 4. APK yükle
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 5. Güncelleme için
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Dosya ile

1. APK'yı telefona kopyala (USB/Bluetooth/Email)
2. Dosya yöneticisinden aç
3. "Bilinmeyen Kaynaklardan Yükleme"ye izin ver
4. Yükle

### QR Kod ile

1. APK'yı bir sunucuya yükle
2. QR kod oluştur
3. Telefonla tara
4. İndir ve yükle

## 🔐 Keystore Yönetimi

### İlk Keystore Oluşturma

```batch
BUILD_RELEASE.bat
# Script otomatik oluşturur
```

### Mevcut Keystore Kullanma

```batch
# Keystore'u MXWPrinter klasörüne koy
# Adı: mxw-release.keystore

# BUILD_RELEASE.bat çalıştır
# Şifreyi gir
```

### Keystore Yedekleme

**ÇOK ÖNEMLİ!**

```batch
# Keystore'u yedekle
copy mxw-release.keystore D:\Yedek\

# Şifreyi kaydet
# Kaybedersen uygulama güncelleyemezsin!
```

## 📊 Build Süreleri

| İşlem | İlk Build | Sonraki Build |
|-------|-----------|---------------|
| Clean | 10-30 sn | 10-30 sn |
| Debug | 2-5 dk | 30-90 sn |
| Release | 3-7 dk | 1-2 dk |

## 🎨 Özelleştirme

### Uygulama Adı

`app/src/main/res/values/strings.xml`
```xml
<string name="app_name">Yeni İsim</string>
```

### Paket Adı

`app/build.gradle.kts`
```kotlin
namespace = "com.yeni.paket"
```

### Sürüm

`app/build.gradle.kts`
```kotlin
versionCode = 2
versionName = "1.1"
```

### İkon

`app/src/main/res/mipmap-*/ic_launcher.png` dosyalarını değiştir

## 🚀 Otomatik Build

### Batch Script

```batch
@echo off
cd C:\print\TiMini-Print\MXWPrinter
call BUILD_WINDOWS.bat
copy app\build\outputs\apk\debug\app-debug.apk D:\APKs\MXW-%date:~-4,4%%date:~-7,2%%date:~-10,2%.apk
```

### Zamanlanmış Görev

1. Görev Zamanlayıcı'yı aç
2. Yeni görev oluştur
3. Script'i seç
4. Zamanı ayarla

## 📈 Build Optimizasyonu

### Gradle Ayarları

`gradle.properties`
```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
```

### Hızlı Build

```batch
# Sadece değişen dosyaları derle
gradlew.bat assembleDebug --no-build-cache
```

## 🎯 Sonraki Adımlar

1. ✅ APK oluştur
2. ✅ Telefonda test et
3. ✅ Hataları düzelt
4. ✅ Release APK oluştur
5. ✅ Play Store'a yükle

## 💡 Pro İpuçları

### Hızlı Test

```batch
# Build + Install + Run
gradlew.bat installDebug
adb shell am start -n com.mxw.printer/.MainActivity
```

### Log İzleme

```batch
# Uygulama loglarını izle
adb logcat | findstr "MXW"
```

### APK Analizi

```batch
# APK boyutunu analiz et
gradlew.bat analyzeDebugBundle
```

## 🆘 Yardım

### Hata Mesajları

**"Execution failed for task ':app:mergeDebugResources'"**
- Çözüm: `gradlew.bat clean`

**"Could not resolve all dependencies"**
- Çözüm: İnternet bağlantısını kontrol et

**"SDK location not found"**
- Çözüm: Android Studio'yu yükle veya `local.properties` oluştur

### Destek

- GitHub Issues
- Stack Overflow
- Android Developers

## ✅ Checklist

Build öncesi:
- [ ] Java 17 yüklü
- [ ] İnternet bağlantısı var
- [ ] Disk alanı yeterli (min 5GB)
- [ ] MXWPrinter klasöründeyim

Build sonrası:
- [ ] APK oluştu
- [ ] Boyut normal (6-12 MB)
- [ ] Telefonda test edildi
- [ ] Tüm özellikler çalışıyor

Release için:
- [ ] Keystore oluşturuldu
- [ ] Keystore yedeklendi
- [ ] Şifre kaydedildi
- [ ] Release APK test edildi

---

**Başarılar! 🎉**

Sorularınız için: GitHub Issues
