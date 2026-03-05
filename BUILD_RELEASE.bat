@echo off
chcp 65001 >nul
echo ========================================
echo MXW01 Printer - Release APK Builder
echo ========================================
echo.

echo Bu script RELEASE (imzalı) APK oluşturur.
echo Google Play Store'a yüklemek için gereklidir.
echo.
echo Devam etmek istiyor musunuz? (E/H)
set /p CONTINUE=
if /i not "%CONTINUE%"=="E" exit /b 0

echo.
echo [1/5] Keystore kontrolü...

set KEYSTORE_FILE=mxw-release.keystore
set KEY_ALIAS=mxwprinter

if not exist "%KEYSTORE_FILE%" (
    echo.
    echo Keystore bulunamadı. Yeni keystore oluşturuluyor...
    echo.
    echo Lütfen aşağıdaki bilgileri girin:
    echo.
    
    keytool -genkey -v -keystore %KEYSTORE_FILE% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity 10000
    
    if errorlevel 1 (
        echo.
        echo HATA: Keystore oluşturulamadı!
        echo JDK yüklü olduğundan emin olun.
        pause
        exit /b 1
    )
    
    echo.
    echo ✓ Keystore oluşturuldu: %KEYSTORE_FILE%
    echo.
    echo ÖNEMLİ: Bu dosyayı ve şifresini GÜVENLİ bir yerde saklayın!
    echo Kaybederseniz uygulama güncellemesi yapamazsınız!
    echo.
    pause
) else (
    echo ✓ Keystore bulundu: %KEYSTORE_FILE%
)

echo.
echo [2/5] Keystore şifresi...
set /p KEYSTORE_PASSWORD=Keystore şifresini girin: 

echo.
echo [3/5] Eski build dosyaları temizleniyor...
call gradlew.bat clean

echo.
echo [4/5] Release APK oluşturuluyor...
echo Bu işlem 3-7 dakika sürebilir...
echo.

call gradlew.bat assembleRelease

if errorlevel 1 (
    echo.
    echo HATA: Release APK oluşturulamadı!
    pause
    exit /b 1
)

echo.
echo [5/5] APK imzalanıyor...

set UNSIGNED_APK=app\build\outputs\apk\release\app-release-unsigned.apk
set SIGNED_APK=app\build\outputs\apk\release\MXWPrinter-release.apk

if exist "%UNSIGNED_APK%" (
    jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore %KEYSTORE_FILE% -storepass %KEYSTORE_PASSWORD% "%UNSIGNED_APK%" %KEY_ALIAS%
    
    if errorlevel 1 (
        echo HATA: İmzalama başarısız!
        pause
        exit /b 1
    )
    
    echo.
    echo APK optimize ediliyor...
    zipalign -v 4 "%UNSIGNED_APK%" "%SIGNED_APK%"
    
    if errorlevel 1 (
        echo UYARI: Zipalign başarısız, unsigned APK kullanılacak
        copy "%UNSIGNED_APK%" "%SIGNED_APK%"
    )
)

echo.
echo ========================================
echo ✓ RELEASE APK HAZIR!
echo ========================================
echo.
echo APK Konumu:
echo %CD%\%SIGNED_APK%
echo.
echo Bu APK:
echo ✓ İmzalı ve optimize edilmiş
echo ✓ Google Play Store'a yüklenebilir
echo ✓ Güvenli dağıtım için hazır
echo.

echo APK'yı masaüstüne kopyalamak ister misiniz? (E/H)
set /p COPY_CHOICE=

if /i "%COPY_CHOICE%"=="E" (
    copy "%SIGNED_APK%" "%USERPROFILE%\Desktop\MXWPrinter-release.apk"
    echo ✓ APK masaüstüne kopyalandı
)

echo.
echo ========================================
echo Sonraki Adımlar:
echo ========================================
echo.
echo 1. APK'yı test edin
echo 2. Google Play Console'a gidin
echo 3. Yeni sürüm oluşturun
echo 4. APK'yı yükleyin
echo 5. Yayınlayın
echo.
pause
