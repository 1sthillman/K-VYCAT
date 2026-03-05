# 🔧 Sorun Giderme Kılavuzu

## ❌ Yazdırma Çalışmıyor

### Adım 1: Bağlantı Kontrolü

#### Log Ekranını Kontrol Edin
1. Ana ekranda **"Log"** butonuna tıklayın
2. Şu mesajları arayın:
   - ✅ `"Bağlandı!"` - Bağlantı başarılı
   - ✅ `"Servisler bulundu"` - UUID'ler doğru
   - ✅ `"Yazıcı hazır!"` - Yazdırmaya hazır
   - ❌ `"HATA: Bağlı değil!"` - Bağlantı yok
   - ❌ `"Servis bulunamadı"` - UUID sorunu

#### Bağlantı Testi
```kotlin
// BleManager.kt içinde
fun testConnection(): Boolean {
    return gatt != null && 
           cmdChar != null && 
           dataChar != null &&
           _connectionState.value == ConnectionState.CONNECTED
}
```

---

### Adım 2: İzin Kontrolü

#### Gerekli İzinler (Android 12+)
```
✅ Bluetooth Tarama (BLUETOOTH_SCAN)
✅ Bluetooth Bağlantı (BLUETOOTH_CONNECT)
✅ Konum (ACCESS_FINE_LOCATION)
```

#### İzinleri Kontrol Et
```kotlin
// Ayarlar → Uygulamalar → MXW Printer → İzinler
// Tüm izinlerin "İzin verildi" olduğundan emin olun
```

#### Manuel İzin Verme
1. Telefon **Ayarlar** → **Uygulamalar**
2. **MXW Printer** bulun
3. **İzinler** → Tüm izinleri **İzin Ver**
4. **Konum** → **Her Zaman İzin Ver** veya **Sadece Uygulama Kullanımda**

---

### Adım 3: Bluetooth ve Konum Servisleri

#### Bluetooth Açık mı?
```
Ayarlar → Bluetooth → AÇIK
```

#### Konum Servisleri Açık mı?
```
Ayarlar → Konum → AÇIK
```

**ÖNEMLİ:** Android'de BLE tarama için konum servisleri AÇIK olmalı!

---

### Adım 4: Yazıcı Kontrolü

#### Yazıcı Açık mı?
- Yazıcının güç düğmesine basın
- LED yanıyor mu kontrol edin
- Kağıt var mı kontrol edin

#### Yazıcı Eşleşme Modunda mı?
- Bazı yazıcılar eşleşme moduna geçmek için özel tuş kombinasyonu gerektirir
- MXW01 için: Genellikle otomatik eşleşme modunda

#### Yazıcı Mesafesi
- Telefon ile yazıcı arası **maksimum 5 metre** olmalı
- Aralarında kalın duvar olmamalı

---

### Adım 5: Protokol Kontrolü

#### Doğru UUID'ler Kullanılıyor mu?

```kotlin
// PrinterProtocol.kt
val CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"  // Komut
val DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb" // Veri
```

#### Komut Sırası Doğru mu?

```kotlin
// BleManager.kt - print() fonksiyonu
1. START   (2221A70000000000)
2. CONFIG1 (2221B10001000000FF)
3. CONFIG2 (2221A10001000000FF)
4. HEAT    (2221A2000100FFFFFF)
5. HEADER  (2221A9000400000230000000)
6. DATA    (satır satır, 48 byte)
7. END     (2221AD000100000000)
```

---

### Adım 6: Test Yazdırma

#### Basit Test Metni
1. **Metin** ekranına gidin
2. Kısa bir metin yazın: `"TEST"`
3. **Yazdır** butonuna tıklayın
4. Log ekranını izleyin

#### Beklenen Log Çıktısı
```
[14:30:15] Yazdırma başlıyor...
[14:30:15] 144 satır gönderiliyor...
[14:30:18] ✅ Yazdırma tamamlandı!
```

---

### Adım 7: Yazıcıyı Sıfırlama

#### Yazıcı Sıfırlama (Hard Reset)
1. Yazıcıyı **kapatın**
2. **10 saniye** bekleyin
3. Yazıcıyı **açın**
4. Uygulamayı **yeniden başlatın**

#### Uygulama Önbelleğini Temizleme
```
Ayarlar → Uygulamalar → MXW Printer → Depolama → Önbelleği Temizle
```

---

## 🐛 Yaygın Hatalar ve Çözümleri

### Hata 1: "HATA: Bağlı değil!"

**Sebep:** Yazıcıya bağlantı kurulamadı

**Çözüm:**
1. Ana ekranda **"Bağlan"** butonuna tıklayın
2. Yazıcıyı listeden seçin
3. "Bağlandı" mesajını bekleyin

---

### Hata 2: "Servis bulunamadı"

**Sebep:** UUID'ler yanlış veya yazıcı uyumlu değil

**Çözüm:**
1. Yazıcı modelini kontrol edin (MXW01 olmalı)
2. Yazıcıyı yeniden başlatın
3. Uygulamayı yeniden başlatın

---

### Hata 3: "Tarama başarısız"

**Sebep:** İzinler verilmedi veya Bluetooth kapalı

**Çözüm:**
1. Bluetooth'u açın
2. Konum servislerini açın
3. Uygulama izinlerini kontrol edin
4. Uygulamayı yeniden başlatın

---

### Hata 4: Yazıcı Listede Görünmüyor

**Sebep:** Yazıcı kapalı veya çok uzakta

**Çözüm:**
1. Yazıcının açık olduğundan emin olun
2. Telefonu yazıcıya yaklaştırın (1-2 metre)
3. Taramayı tekrar deneyin
4. Yazıcıyı yeniden başlatın

---

### Hata 5: Bağlantı Hemen Kesiliyor

**Sebep:** Sinyal zayıf veya pil düşük

**Çözüm:**
1. Telefonu yazıcıya yaklaştırın
2. Yazıcı pilini şarj edin
3. Bluetooth'u kapat-aç yapın
4. Telefonu yeniden başlatın

---

## 📱 Debug Modu

### Log Ekranını Kullanma

1. Ana ekranda **"Log"** butonuna tıklayın
2. Tüm işlemleri takip edin
3. Hata mesajlarını not alın

### Örnek Log Analizi

#### ✅ Başarılı Yazdırma
```
[14:30:10] Tarama başlatıldı...
[14:30:12] Cihaz: MXW01 (48:0F:57:3E:60:77) RSSI:-45
[14:30:15] Bağlanıyor: MXW01
[14:30:16] Bağlandı! Servisler keşfediliyor...
[14:30:17] Servisler bulundu
[14:30:17] Yazıcı hazır!
[14:30:20] Yazdırma başlıyor...
[14:30:20] 144 satır gönderiliyor...
[14:30:23] ✅ Yazdırma tamamlandı!
```

#### ❌ Başarısız Bağlantı
```
[14:30:10] Tarama başlatıldı...
[14:30:20] Tarama bitti. 0 cihaz bulundu.
[14:30:25] HATA: Yazıcı bulunamadı!
```

**Çözüm:** Yazıcı kapalı veya çok uzakta

---

## 🔍 Detaylı Test Adımları

### Test 1: Bluetooth Tarama
```
1. Ana ekran → "Tara" butonu
2. İzinleri kabul et
3. 10 saniye bekle
4. Yazıcı listede görünmeli
```

**Beklenen:** MXW01 cihazı listede, RSSI > -80 dBm

---

### Test 2: Bağlantı
```
1. Yazıcıyı listeden seç
2. "Bağlanıyor..." mesajı
3. 3-5 saniye bekle
4. "Bağlandı!" mesajı
```

**Beklenen:** Ana ekranda yeşil "Bağlı" durumu

---

### Test 3: Yazdırma
```
1. Metin ekranına git
2. "TEST" yaz
3. "Yazdır" butonuna tıkla
4. İlerleme çubuğunu izle
```

**Beklenen:** Yazıcıdan "TEST" çıktısı

---

## 📞 Destek

### Log Dosyası Paylaşma

1. Log ekranını aç
2. Tüm logları kopyala
3. Destek ekibine gönder

### Sistem Bilgileri

```
Uygulama: MXW Printer v1.0
Android Sürümü: [Ayarlar → Telefon Hakkında]
Bluetooth Sürümü: BLE 4.0+
Yazıcı Modeli: MXW01
```

---

## ✅ Kontrol Listesi

Yazdırma yapmadan önce kontrol edin:

- [ ] Bluetooth AÇIK
- [ ] Konum servisleri AÇIK
- [ ] Uygulama izinleri VERİLDİ
- [ ] Yazıcı AÇIK
- [ ] Yazıcı YAKIN (1-5 metre)
- [ ] Kağıt VAR
- [ ] Pil DOLU
- [ ] Uygulama yazıcıya BAĞLI
- [ ] Log ekranında "Yazıcı hazır!" mesajı VAR

Tüm maddeler ✅ ise yazdırma çalışmalıdır!

---

## 🚀 Hızlı Çözüm

**En yaygın sorun:** İzinler verilmemiş

**Hızlı çözüm:**
```
1. Ayarlar → Uygulamalar → MXW Printer
2. İzinler → TÜM İZİNLERİ VER
3. Bluetooth ve Konum AÇIK
4. Uygulamayı yeniden başlat
5. Yazıcıyı tara ve bağlan
```

Bu adımlar %90 sorunu çözer! 🎉
