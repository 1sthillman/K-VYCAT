# MXW01 Printer - Kivy Mobile App

Bluetooth termal yazıcı kontrolü için mobil uygulama.

## Özellikler

- ✅ Metin yazdırma
- ✅ QR kod oluşturma
- ✅ Bluetooth tarama
- ✅ Python kodundaki AYNI protokol

## APK İndirme

1. [Actions](../../actions) sekmesine git
2. En son başarılı build'i seç
3. "MXW01-Printer-APK" artifact'ini indir
4. ZIP'i aç
5. APK'yı telefona yükle

## Kullanım

1. Uygulamayı aç
2. İzinleri ver (Bluetooth, Konum)
3. "Yazıcı Bağla" → MXW01 seç
4. Yazdırmaya başla!

## Build

GitHub Actions otomatik build yapar. Her push'ta yeni APK oluşturulur.

## Protokol

```python
CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"
DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb"

CMD_START = bytes.fromhex("2221A70000000000")
CMD_CONFIG1 = bytes.fromhex("2221B10001000000FF")
CMD_CONFIG2 = bytes.fromhex("2221A10001000000FF")
CMD_HEAT = bytes.fromhex("2221A2000100FFFFFF")
CMD_HEADER = bytes.fromhex("2221A9000400000230000000")
CMD_END = bytes.fromhex("2221AD000100000000")
```

## Lisans

MIT
