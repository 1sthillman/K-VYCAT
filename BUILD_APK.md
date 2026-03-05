# APK Derleme Kılavuzu

## 🔨 APK Nasıl Derlenir?

### Yöntem 1: Android Studio ile (Önerilen)

1. **Android Studio'yu açın**
2. **"Open"** → `TiMini-Print/MXWPrinter` klasörünü seçin
3. Gradle sync tamamlanmasını bekleyin
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK hazır olduğunda bildirim gelecek
6. **"locate"** butonuna tıklayın

**APK Konumu:**
```
TiMini-Print/MXWPrinter/app/build/outputs/apk/debug/app-debug.apk
```

---

### Yöntem 2: Komut Satırı ile

#### Windows:
```cmd
cd TiMini-Print\MXWPrinter
gradlew.bat assembleDebug
```

#### Linux/Mac:
```bash
cd TiMini-Print/MXWPrinter
./gradlew assembleDebug
```

**APK Konumu:**
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 APK Yükleme

### Telefona Yükleme:

1. **USB ile:**
   ```cmd
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Dosya Paylaşımı ile:**
   - APK'yı telefona kopyalayın
   - Dosya yöneticisinden açın
   - "Bilinmeyen kaynaklardan yükleme" iznini verin
   - Yükle'ye tıklayın

---

## 🚀 Release APK (İmzalı)

Production için imzalı APK:

1. **Keystore oluşturun:**
   ```cmd
   keytool -genkey -v -keystore mxw-printer.keystore -alias mxw -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **build.gradle.kts'e ekleyin:**
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("mxw-printer.keystore")
               storePassword = "your-password"
               keyAlias = "mxw"
               keyPassword = "your-password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               isMinifyEnabled = true
               proguardFiles(...)
           }
       }
   }
   ```

3. **Release APK derleyin:**
   ```cmd
   gradlew assembleRelease
   ```

**Release APK Konumu:**
```
app/build/outputs/apk/release/app-release.apk
```

---

## ✅ Derleme Gereksinimleri

- ✅ **JDK 11** veya üzeri
- ✅ **Android SDK** (API 26-35)
- ✅ **Gradle 8.x**
- ✅ **Android Studio** (isteğe bağlı)

---

## 🔍 Sorun Giderme

### "SDK not found"
```cmd
# Android SDK yolunu ayarlayın
set ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk
```

### "Gradle sync failed"
```cmd
# Gradle wrapper'ı güncelleyin
gradlew wrapper --gradle-version 8.7
```

### "Build failed"
1. `Build → Clean Project`
2. `Build → Rebuild Project`
3. Gradle cache'i temizleyin:
   ```cmd
   gradlew clean
   ```

---

## 📦 APK Boyutu

- **Debug APK:** ~5-8 MB
- **Release APK (minified):** ~3-5 MB

---

## 🎯 Hızlı Komutlar

```cmd
# Debug APK
gradlew assembleDebug

# Release APK
gradlew assembleRelease

# Temizle
gradlew clean

# Yükle (USB bağlı telefon)
gradlew installDebug

# Tümü (temizle + derle + yükle)
gradlew clean assembleDebug installDebug
```

---

## ✨ Başarılı Derleme Sonrası

APK hazır! Şimdi:
1. ✅ Telefona yükleyin
2. ✅ İzinleri verin
3. ✅ Yazıcıya bağlanın
4. ✅ Test yazdırın

**Kullanım kılavuzu:** `KULLANIM_KILAVUZU.md`
