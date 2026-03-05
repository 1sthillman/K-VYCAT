@echo off
chcp 65001 >nul
echo ========================================
echo MXW01 Printer - Windows APK Builder
echo ========================================
echo.

echo [1/4] Gradle kontrolü...
if not exist "gradlew.bat" (
    echo HATA: gradlew.bat bulunamadı!
    echo Bu dosyayı MXWPrinter klasöründe çalıştırın.
    pause
    exit /b 1
)

echo ✓ Gradle bulundu
echo.

echo [2/4] Eski build dosyaları temizleniyor...
call gradlew.bat clean
if errorlevel 1 (
    echo HATA: Clean işlemi başarısız!
    pause
    exit /b 1
)

echo ✓ Temizleme tamamlandı
echo.

echo [3/4] APK oluşturuluyor...
echo Bu işlem 2-5 dakika sürebilir, lütfen bekleyin...
echo.

call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo HATA: APK oluşturulamadı!
    echo.
    echo Olası çözümler:
    echo 1. Android Studio'yu açın ve projeyi sync edin
    echo 2. JDK 17 yüklü olduğundan emin olun
    echo 3. İnternet bağlantınızı kontrol edin
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ APK başarıyla oluşturuldu!
echo.

echo [4/4] APK konumu...
echo.

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if exist "%APK_PATH%" (
    echo ========================================
    echo ✓ BAŞARILI!
    echo ========================================
    echo.
    echo APK Konumu:
    echo %CD%\%APK_PATH%
    echo.
    echo APK Boyutu:
    for %%A in ("%APK_PATH%") do echo %%~zA bytes
    echo.
    echo ========================================
    echo Telefona Yükleme:
    echo ========================================
    echo.
    echo 1. USB ile bağlayın
    echo 2. USB Debugging açın
    echo 3. Şu komutu çalıştırın:
    echo    adb install -r "%APK_PATH%"
    echo.
    echo VEYA
    echo.
    echo APK dosyasını telefona kopyalayıp açın
    echo.
    
    echo APK'yı masaüstüne kopyalamak ister misiniz? (E/H)
    set /p COPY_CHOICE=
    
    if /i "%COPY_CHOICE%"=="E" (
        copy "%APK_PATH%" "%USERPROFILE%\Desktop\MXWPrinter.apk"
        if errorlevel 1 (
            echo Kopyalama başarısız!
        ) else (
            echo ✓ APK masaüstüne kopyalandı: MXWPrinter.apk
        )
    )
) else (
    echo HATA: APK dosyası bulunamadı!
    echo Beklenen konum: %APK_PATH%
)

echo.
pause
