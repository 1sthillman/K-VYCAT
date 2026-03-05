# MXW01 Printer - Kivy Android App

Python ile yazılmış MXW01 termal yazıcı kontrolü için Android uygulaması.

## Özellikler

- ✅ Android native Bluetooth Low Energy (pyjnius)
- ✅ Kivy UI framework
- ✅ Masaüstü `printer_app.py` ile aynı protokol
- ✅ LSB encoding
- ✅ Çok satırlı metin desteği
- ✅ Otomatik font boyutlandırma

## Kurulum

1. [Releases](https://github.com/1sthillman/K-VYCAT/releases) sayfasından APK'yı indirin
2. Android cihazınızda "Bilinmeyen Kaynaklardan Yükleme" izni verin
3. APK'yı yükleyin
4. Uygulama açıldığında Bluetooth ve Konum izinleri verin

## Kullanım

1. Yazıcıyı açın (MAC: `48:0F:57:3E:60:77`)
2. Uygulamayı açın
3. Yazdırmak istediğiniz metni girin
4. "Yazdır" butonuna basın
5. Uygulama otomatik olarak yazıcıya bağlanıp yazdıracak

## Teknik Detaylar

### Bluetooth Implementasyonu

Bu uygulama **Android native Bluetooth API** kullanır (`pyjnius` ile):
- `BluetoothAdapter` - Bluetooth adaptörü
- `BluetoothGatt` - GATT bağlantısı
- `BluetoothGattCharacteristic` - Characteristic yazma

**Neden Bleak değil?**
- Bleak, python-for-android ile uyumlu değil
- Windows/Linux bağımlılıkları Android'de çalışmaz
- Android native API daha stabil ve hızlı

### Yazıcı Protokolü

```
Device: 48:0F:57:3E:60:77
CMD UUID: 0000ae01-0000-1000-8000-00805f9b34fb
DATA UUID: 0000ae03-0000-1000-8000-00805f9b34fb
```

**Komut Sırası:**
1. START: `2221A70000000000`
2. CONFIG1: `2221B10001000000FF`
3. CONFIG2: `2221A10001000000FF`
4. HEAT: `2221A2000100FFFFFF`
5. HEADER: `2221A9000400000230000000`
6. DATA: 48 byte/satır (LSB encoding)
7. END: `2221AD000100000000`

### Build

GitHub Actions otomatik olarak APK oluşturur:
- Ubuntu 20.04
- Python 3.9
- Buildozer + python-for-android
- Android SDK 31, NDK r25b

## Geliştirme

```bash
# Yerel build (Linux/WSL2 gerekli)
buildozer android debug

# GitHub Actions ile build
git push origin main
```

## Lisans

MIT
