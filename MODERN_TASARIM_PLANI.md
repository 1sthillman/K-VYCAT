# 🎨 Modern Tasarım Güncelleme Planı

## ✅ Tamamlanan

### 1. Yeni Tema Sistemi
- ✅ Modern renk paleti (Indigo, Purple, Cyan)
- ✅ Profesyonel gradient'ler
- ✅ Gelişmiş tipografi

### 2. Veri Modelleri
- ✅ `PrintSettings.kt` - Tüm yazdırma ayarları
- ✅ `PaperSize` enum - 10 farklı kağıt boyutu
- ✅ `PrintOrientation` - Yatay/Dikey
- ✅ `PaperConsumption` - Kağıt tüketimi hesaplama

### 3. UI Bileşenleri
- ✅ `PrintDialog.kt` - Modern yazdırma dialog'u
- ✅ `PrintingDialog.kt` - Yazdırma progress dialog'u
- ✅ `Selectors.kt` - Tüm selector bileşenleri

---

## 📋 Yapılacaklar

### 1. Görsel Düzenleme Ekranı
```kotlin
ImageEditorScreen.kt
- Galeri entegrasyonu (Android Photo Picker API)
- Kırpma (Crop)
- Döndürme (Rotate)
- Zoom
- Filtreler (Threshold ayarı)
- Önizleme
```

### 2. Gelişmiş Metin Ekranı
```kotlin
AdvancedTextScreen.kt
- Yazı boyutu slider (10-200px)
- Hizalama (Sol, Orta, Sağ)
- Kalın/Normal toggle
- Satır aralığı
- Kenar boşlukları
- Canlı önizleme
```

### 3. Pankart Ekranı (Harf-Harf)
```kotlin
BannerScreen.kt
- Harf-harf büyük çıktı modu
- Yatay/Dikey seçimi
- Tam kağıt boyutu kullanımı
- Her harf ayrı sayfa
- Kağıt tüketimi gösterimi
```

### 4. Yazdırma Sistemi
```kotlin
PrintManager.kt
- Kopya sayısı desteği
- Kağıt boyutu kontrolü
- Yönlendirme (rotate bitmap)
- Progress tracking
- Hata yönetimi
```

### 5. Önizleme Sistemi
```kotlin
PreviewScreen.kt
- Gerçek boyut önizleme
- Zoom in/out
- Kağıt sınırları gösterimi
- Kağıt tüketimi bilgisi
- Düzenleme butonu
```

---

## 🎯 Özellik Detayları

### Kağıt Boyutları
```
✅ 58mm (Sürekli)
✅ 58mm × 40mm
✅ 58mm × 50mm
✅ 58mm × 60mm
✅ 58mm × 80mm
✅ 58mm × 86mm
✅ 80mm (Sürekli)
✅ 80mm × 60mm
✅ 80mm × 80mm
✅ 80mm × 120mm
```

### Yazdırma Ayarları
```
✅ Kağıt boyutu seçimi
✅ Yönlendirme (Dikey/Yatay)
✅ Isı seviyesi (0-100%)
✅ Kopya sayısı (1-99)
✅ Yazı boyutu (Otomatik veya 10-200px)
✅ Hizalama (Sol/Orta/Sağ)
✅ Kalın/Normal
✅ Görsel eşik değeri (0-255)
```

### Görsel Düzenleme
```
📋 Galeri erişimi (Photo Picker API)
📋 Kırpma (Crop to paper size)
📋 Döndürme (90°, 180°, 270°)
📋 Zoom (Pinch to zoom)
📋 Filtreler (B&W threshold)
📋 Yatay/Dikey yazdırma
```

### Pankart Özel Özellikleri
```
📋 Harf-harf büyük çıktı
📋 Her harf tam sayfa
📋 Yatay/Dikey seçimi
📋 Otomatik font boyutu
📋 Kağıt tüketimi: "5 harf × 86mm = 43cm"
```

### Önizleme Özellikleri
```
📋 Gerçek boyut gösterimi
📋 Kağıt sınırları
📋 Zoom kontrolü
📋 Kağıt tüketimi bilgisi
📋 "Düzenle" butonu
📋 "Yazdır" butonu
```

---

## 🎨 UI/UX İyileştirmeleri

### Modern Butonlar
```kotlin
// Primary Button
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Primary,
        contentColor = OnPrimary
    ),
    shape = RoundedCornerShape(12.dp),
    elevation = ButtonDefaults.buttonElevation(4.dp)
)

// Outlined Button
OutlinedButton(
    border = BorderStroke(2.dp, Primary),
    shape = RoundedCornerShape(12.dp)
)

// Icon Button
IconButton(
    modifier = Modifier
        .size(48.dp)
        .background(Primary.copy(0.1f), RoundedCornerShape(12.dp))
)
```

### Modern Kartlar
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = CardSurface
    ),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, CardBorder),
    elevation = CardDefaults.cardElevation(2.dp)
)
```

### Animasyonlar
```kotlin
// Fade in/out
AnimatedVisibility(
    visible = showDialog,
    enter = fadeIn() + scaleIn(),
    exit = fadeOut() + scaleOut()
)

// Slide
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
        slideInHorizontally() + fadeIn() with
        slideOutHorizontally() + fadeOut()
    }
)

// Progress
CircularProgressIndicator(
    progress = { progress },
    color = Primary,
    trackColor = SurfaceVariant
)
```

---

## 📱 Ekran Yapısı

### Ana Ekran
```
┌─────────────────────────────┐
│ MXW Printer                 │
│ ● Bağlı - MXW01        [⚙️] │
├─────────────────────────────┤
│ ┌─────────┐ ┌─────────┐    │
│ │  Metin  │ │ Pankart │    │
│ │   📝    │ │   🎯    │    │
│ └─────────┘ └─────────┘    │
│ ┌─────────┐ ┌─────────┐    │
│ │ Görsel  │ │  Sıra   │    │
│ │   🖼️    │ │   📋    │    │
│ └─────────┘ └─────────┘    │
├─────────────────────────────┤
│ Son Önizleme                │
│ ┌─────────────────────────┐ │
│ │                         │ │
│ │    [Preview Image]      │ │
│ │                         │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

### Metin Ekranı
```
┌─────────────────────────────┐
│ ← Metin Yazdırma            │
├─────────────────────────────┤
│ Metin Girin:                │
│ ┌─────────────────────────┐ │
│ │ Merhaba Dünya!          │ │
│ └─────────────────────────┘ │
│                             │
│ Yazı Boyutu: [━━━●━━━] 48px│
│ Hizalama: [◀] [●] [▶]      │
│ Kalın: [●] Normal: [ ]     │
│                             │
│ ┌─────────────────────────┐ │
│ │    Önizleme             │ │
│ │  Merhaba Dünya!         │ │
│ │                         │ │
│ │ Kağıt: 58mm × 40mm      │ │
│ │ Tüketim: ~4cm           │ │
│ └─────────────────────────┘ │
│                             │
│ [Özelleştir] [Yazdır]      │
└─────────────────────────────┘
```

### Yazdırma Dialog'u
```
┌─────────────────────────────┐
│ Yazdırma Ayarları       [×] │
│ Çıktı özelliklerini ayarlayın│
├─────────────────────────────┤
│ Kağıt Boyutu                │
│ [58mm] [58×40] [58×86] ...  │
│                             │
│ Yönlendirme                 │
│ [● Dikey] [ Yatay]          │
│                             │
│ Isı Seviyesi                │
│ [━━━━━●━━━] 75%             │
│                             │
│ Kopya Sayısı                │
│ [-] [3] [+]                 │
│                             │
│ ▼ Gelişmiş Ayarlar          │
│                             │
│ ┌─────────────────────────┐ │
│ │ Tahmini Kağıt Tüketimi  │ │
│ │ 12 cm              📄   │ │
│ └─────────────────────────┘ │
│                             │
│ [İptal] [🖨️ Yazdır]        │
└─────────────────────────────┘
```

### Yazdırma Progress
```
┌─────────────────────────────┐
│                             │
│        ┌─────────┐          │
│        │   🖨️    │          │
│        └─────────┘          │
│                             │
│      Yazdırılıyor           │
│      %67 tamamlandı         │
│                             │
│ [━━━━━━━━━━━━━━━━━━━━━━━] │
│ Başladı          Tamamlanıyor│
│                             │
│        [İptal]              │
└─────────────────────────────┘
```

---

## 🔧 Teknik Implementasyon

### 1. Galeri Entegrasyonu
```kotlin
// Photo Picker API (Android 13+)
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri ->
    uri?.let { viewModel.selectImage(it) }
}

Button(onClick = {
    launcher.launch(
        PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
        )
    )
}) {
    Text("Galeri")
}
```

### 2. Görsel Kırpma
```kotlin
// Cropper library veya custom implementation
ImageCropper(
    bitmap = originalBitmap,
    aspectRatio = paperSize.widthPx.toFloat() / paperSize.getHeightPx()!!,
    onCrop = { croppedBitmap ->
        viewModel.updateImage(croppedBitmap)
    }
)
```

### 3. Bitmap Döndürme
```kotlin
fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(
        bitmap, 0, 0,
        bitmap.width, bitmap.height,
        matrix, true
    )
}
```

### 4. Kağıt Tüketimi Hesaplama
```kotlin
fun calculateConsumption(
    heightPx: Int,
    paperWidthPx: Int,
    copies: Int
): PaperConsumption {
    val heightMm = (heightPx * 58) / paperWidthPx
    return PaperConsumption(
        heightMm = heightMm,
        heightPx = heightPx,
        copies = copies
    )
}
```

### 5. Kopya Yazdırma
```kotlin
suspend fun printWithCopies(
    bitmap: Bitmap,
    settings: PrintSettings
) {
    repeat(settings.copies) { copy ->
        addLog("Kopya ${copy + 1}/${settings.copies}")
        print(bitmap, settings.heatLevel)
        if (copy < settings.copies - 1) {
            delay(2000) // Kopyalar arası bekleme
        }
    }
}
```

---

## 📊 Performans Optimizasyonları

### 1. Bitmap Caching
```kotlin
private val bitmapCache = LruCache<String, Bitmap>(10)

fun getCachedBitmap(key: String): Bitmap? {
    return bitmapCache.get(key)
}

fun cacheBitmap(key: String, bitmap: Bitmap) {
    bitmapCache.put(key, bitmap)
}
```

### 2. Lazy Loading
```kotlin
val preview by remember {
    derivedStateOf {
        generatePreview(text, settings)
    }
}
```

### 3. Coroutine Optimization
```kotlin
viewModelScope.launch(Dispatchers.Default) {
    val bitmap = generateBitmap(text, settings)
    withContext(Dispatchers.Main) {
        _previewBitmap.value = bitmap
    }
}
```

---

## ✅ Sonraki Adımlar

1. **Görsel düzenleme ekranını tamamla**
   - Photo Picker entegrasyonu
   - Kırpma UI
   - Döndürme butonları

2. **Pankart harf-harf özelliğini ekle**
   - Her harf ayrı bitmap
   - Tam sayfa kullanımı
   - Kağıt tüketimi hesaplama

3. **Tüm ekranları güncelle**
   - Yeni tema kullan
   - Modern butonlar
   - Animasyonlar ekle

4. **Test ve optimize et**
   - Performans testleri
   - UI testleri
   - Gerçek cihazda test

---

## 🎉 Beklenen Sonuç

**Profesyonel, modern, tam özellikli bir yazdırma uygulaması!**

- ✅ 10 farklı kağıt boyutu
- ✅ Yatay/Dikey yazdırma
- ✅ Gelişmiş görsel düzenleme
- ✅ Harf-harf pankart
- ✅ Kağıt tüketimi hesaplama
- ✅ Kopya sayısı
- ✅ Her yazdırma için ısı ayarı
- ✅ Modern, profesyonel tasarım
- ✅ Smooth animasyonlar
- ✅ Optimize performans

**Kullanıcı deneyimi mükemmel olacak!** 🎊
