@echo off
echo ========================================
echo MXW Printer - APK Derleme
echo ========================================
echo.

echo [1/3] Temizleniyor...
call gradlew.bat clean

echo.
echo [2/3] APK derleniyor...
call gradlew.bat assembleDebug

echo.
echo [3/3] Tamamlandi!
echo.
echo APK konumu:
echo app\build\outputs\apk\debug\app-debug.apk
echo.

echo Telefona yuklemek icin:
echo adb install app\build\outputs\apk\debug\app-debug.apk
echo.

pause
