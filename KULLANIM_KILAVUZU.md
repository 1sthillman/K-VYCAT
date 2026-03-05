# MXW Printer - Android Uygulama Kullanım Kılavuzu

## 📱 Uygulama Özellikleri

Bu Android uygulaması, MXW01 termal yazıcınızı Bluetooth üzerinden kontrol etmenizi sağlar.

### ✨ Özellikler
- ✅ Bluetooth LE ile otomatik bağlantı
- ✅ Metin yazdırma (otomatik font boyutu)
- ✅ Pankart/Afiş modu (büyük yazılar)
- ✅ Görsel yazdırma (fotoğraf, logo)
- ✅ Çoklu yazdırma sırası
- ✅ Canlı önizleme
- ✅ Isı seviyesi ayarı
- ✅ Sistem günlüğü

---

## 🚀 İlk Kurulum

### 1. APK Yükleme
```bash
# Android Studio ile derleme
cd TiMini-Print/MXWPrinter
./gradlew assembleDebug

# APK konumu:
# app/build/outputs/apk/debug/app-debug.apk
```

### 2. İzinler
Uygulama ilk açılışta şu izinleri isteyecek:
- ✅ **Bluetooth** - Yazıcıya bağlanmak için
- ✅ **Konum** - BLE tarama için (Android <12)
- ✅ **Depolama** - Görsel yazdırmak için

**ÖNEMLİ:** Tüm izinleri verin, aksi halde uygulama çalışmaz!

---

## 📖 Kullanım Adımları

### 1️⃣ Yazıcıya Bağlanma

1. Uygulamayı açın
2. Ana ekranda **"Bağlan"** butonuna tıklayın
3. Yazıcınızı açın (MXW01)
4. Tarama ekranında yazıcınızı seçin
5. Bağlantı durumu **"Bağlı"** olarak görünecek

**Yazıcı Adresi:** `48:0F:57:3E:60:77`

---

### 2️⃣ Metin Yazdırma

1. Ana ekranda **"Metin"** kartına tıklayın
2. Yazdırmak istediğiniz metni yazın
3. Önizlemeyi kontrol edin
4. **"Yazdır"** butonuna tıklayın

**Özellikler:**
- Otomatik font boyutu (yazı genişliğe sığar)
- Hizalama: Sol, Orta, Sağ
- Kalın/Normal yazı
- Canlı önizleme

---

### 3️⃣ Pankart/Afiş Yazdırma

1. Ana ekranda **"Pankart"** kartına tıklayın
2. Büyük yazınızı girin
3. Yükseklik ayarını yapın (varsayılan: 120px)
4. **"Yazdır"** butonuna tıklayın

**Kullanım Alanları:**
- Duyurular
- Başlıklar
- Dikkat çekici mesajlar

---

### 4️⃣ Görsel Yazdırma

1. Ana ekranda **"Görsel"** kartına tıklayın
2. **"Görsel Seç"** butonuna tıklayın
3. Galerinizden fotoğraf seçin
4. Eşik değerini ayarlayın (varsayılan: 128)
   - Düşük değer = Daha koyu
   - Yüksek değer = Daha açık
5. Önizlemeyi kontrol edin
6. **"Yazdır"** butonuna tıklayın

**İpuçları:**
- Yüksek kontrastlı görseller daha iyi çıkar
- Logo ve QR kodlar için eşik: 128
- Fotoğraflar için eşik: 100-140 arası deneyin

---

### 5️⃣ Çoklu Yazdırma (Sıra)

Birden fazla öğeyi tek seferde yazdırın:

1. Ana ekranda **"Sıra"** kartına tıklayın
2. **"Öğe Ekle"** butonuna tıklayın
3. Eklemek istediğiniz öğe türünü seçin:
   - Metin
   - Pankart
   - Görsel
   - Çizgi (ayırıcı)
   - Boşluk
4. Sırayı düzenleyin (sürükle-bırak)
5. **"Tümünü Yazdır"** butonuna tıklayın

**Örnek Kullanım:**
```
1. Pankart: "FIRSATLAR"
2. Çizgi (ayırıcı)
3. Metin: "Ürün 1 - 50 TL"
4. Metin: "Ürün 2 - 75 TL"
5. Boşluk (3 satır)
6. Görsel: QR Kod
```

---

## ⚙️ Ayarlar

### Isı Seviyesi
- **0-25:** Çok düşük (test için)
- **50:** Orta (normal kullanım)
- **75:** Yüksek (önerilen) ✅
- **100:** Maksimum (koyu yazdırma)

**Öneri:** 75 ile başlayın, gerekirse artırın.

### Yazdırma Hızı
- Satır başına bekleme süresi (ms)
- Varsayılan: 50ms
- Daha yavaş = Daha kaliteli (ama uzun sürer)

---

## 🔧 Sorun Giderme

### Yazıcı Bulunamıyor
1. Yazıcının açık olduğundan emin olun
2. Bluetooth'u kapatıp açın
3. Konum servislerinin açık olduğunu kontrol edin
4. Uygulamayı yeniden başlatın

### Bağlantı Kesiliyor
1. Yazıcıyı telefonun yakınında tutun (1-2 metre)
2. Diğer Bluetooth cihazlarını kapatın
3. Yazıcıyı yeniden başlatın

### Yazılar Bulanık/Çift Görünüyor
- Bu OLMAMALI! Uygulama doğru kodlanmış.
- Eğer oluyorsa:
  1. Yazıcıyı yeniden başlatın
  2. Uygulamayı kapatıp açın
  3. Isı seviyesini 75'e ayarlayın

### Kağıt İlerliyor Ama Yazmıyor
- Isı seviyesini artırın (75 → 100)
- Termal kağıt kullandığınızdan emin olun

---

## 📊 Sistem Günlüğü

Sorun yaşıyorsanız:
1. Ana ekranda **"Sistem Günlüğü"** butonuna tıklayın
2. Hata mesajlarını kontrol edin
3. Günlüğü paylaşarak destek alabilirsiniz

---

## 🎯 Hızlı İpuçları

### En İyi Sonuçlar İçin:
1. ✅ Isı seviyesi: 75
2. ✅ Termal kağıt kullanın (58mm)
3. ✅ Yazıcıyı yakında tutun
4. ✅ Önizlemeyi kontrol edin
5. ✅ Yüksek kontrastlı görseller kullanın

### Pil Tasarrufu:
- Kullanmadığınızda bağlantıyı kesin
- Yazıcıyı kapatın

### Hızlı Yazdırma:
- Metin için: Doğrudan "Metin" ekranını kullanın
- Karmaşık belgeler için: "Sıra" özelliğini kullanın

---

## 📝 Teknik Detaylar

### Protokol Bilgileri
- **Yazıcı Modeli:** MXW01
- **Genişlik:** 384 piksel (48mm)
- **Satır başına byte:** 48
- **Encoding:** LSB first
- **UUID (Komut):** `0000ae01-0000-1000-8000-00805f9b34fb`
- **UUID (Data):** `0000ae03-0000-1000-8000-00805f9b34fb`

### Komut Sırası
```
1. START   (2221A70000000000)
2. CONFIG1 (2221B10001000000FF)
3. CONFIG2 (2221A10001000000FF)
4. HEAT    (2221A2000100FFFFFF)
5. HEADER  (2221A9000400000230000000)
6. DATA    (48 byte x N satır)
7. END     (2221AD000100000000)
```

---

## 🆘 Destek

Sorun yaşıyorsanız:
1. Bu kılavuzu okuyun
2. Sistem günlüğünü kontrol edin
3. Yazıcıyı ve uygulamayı yeniden başlatın
4. Hala sorun varsa: Günlük dosyasını paylaşın

---

## ✅ Başarılı Kullanım Kontrol Listesi

- [ ] İzinler verildi
- [ ] Yazıcı açık ve yakında
- [ ] Bluetooth açık
- [ ] Konum servisleri açık (Android <12)
- [ ] Termal kağıt takılı
- [ ] Isı seviyesi 75
- [ ] Önizleme kontrol edildi
- [ ] Bağlantı durumu "Bağlı"

Hepsi ✅ ise yazdırmaya hazırsınız! 🎉

---

**Not:** Bu uygulama, çalışan Python kodundan (print.md) birebir dönüştürülmüştür. Aynı protokol, aynı encoding, aynı komutlar kullanılmaktadır.
