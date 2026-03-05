# Bluetooth Tarama ve Bağlantı Kılavuzu

## 📱 Android Bluetooth İzinleri

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Bluetooth İzinleri -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
    <!-- Android 12+ (API 31+) için yeni izinler -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    
    <!-- Konum izni (Android 12 altı için gerekli) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Bluetooth LE özelliği -->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />

    <application
        android:name=".MXWPrinterApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MXWPrinter">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 🔍 BleManager.kt - Bluetooth Tarama

### 1. İzin Kontrolü ve Tarama Başlatma

```kotlin
package com.mxw.printer.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class BleManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private var scanJob: Job? = null

    // İzin kontrolü
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 11 ve altı
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Bluetooth açık mı?
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    // Tarama başlat
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasBluetoothPermissions()) {
            addLog("HATA: Bluetooth izinleri verilmedi!")
            return
        }

        if (!isBluetoothEnabled()) {
            addLog("HATA: Bluetooth kapalı!")
            return
        }

        if (_isScanning.value) {
            addLog("Tarama zaten devam ediyor...")
            return
        }

        _scanResults.value = emptyList()
        _isScanning.value = true
        addLog("🔍 Bluetooth tarama başladı...")

        // Tarama filtreleri (opsiyonel - tüm cihazları taramak için null bırakabilirsiniz)
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // Hızlı tarama
            .build()

        // Filtre yok - tüm BLE cihazlarını tara
        val filters = emptyList<ScanFilter>()

        bleScanner?.startScan(filters, scanSettings, scanCallback)

        // 10 saniye sonra otomatik durdur
        scanJob = CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            stopScan()
        }
    }

    // Tarama durdur
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!_isScanning.value) return

        bleScanner?.stopScan(scanCallback)
        _isScanning.value = false
        scanJob?.cancel()
        addLog("⏹️ Tarama durduruldu. ${_scanResults.value.size} cihaz bulundu.")
    }

    // Tarama callback
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Bilinmeyen"
            val deviceAddress = device.address
            val rssi = result.rssi

            // Zaten listede var mı kontrol et
            val currentList = _scanResults.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.device.address == deviceAddress }

            if (existingIndex >= 0) {
                // Güncelle (RSSI değişmiş olabilir)
                currentList[existingIndex] = result
            } else {
                // Yeni cihaz ekle
                currentList.add(result)
                addLog("📱 Bulundu: $deviceName ($deviceAddress) - $rssi dBm")
            }

            _scanResults.value = currentList.sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            val errorMsg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "Tarama zaten başlatılmış"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "Uygulama kaydı başarısız"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "Özellik desteklenmiyor"
                SCAN_FAILED_INTERNAL_ERROR -> "İç hata"
                else -> "Bilinmeyen hata ($errorCode)"
            }
            addLog("❌ Tarama hatası: $errorMsg")
        }
    }

    // Log mesajları
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages

    fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _logMessages.value = _logMessages.value + "[$timestamp] $message"
    }
}
```

---

## 📱 ScanScreen.kt - Tarama Ekranı

```kotlin
package com.mxw.printer.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.mxw.printer.model.PrinterViewModelNew
import com.mxw.printer.ui.components.*
import com.mxw.printer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(vm: PrinterViewModelNew, onBack: () -> Unit) {
    val scanResults by vm.scanResults.collectAsState()
    val isScanning by vm.isScanning.collectAsState()

    // İzin listesi - Android sürümüne göre
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (API 31+)
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        // Android 11 ve altı
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // İzin isteme launcher
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Tüm izinler verildi mi?
        if (results.values.all { it }) {
            vm.startScan()
        } else {
            // İzin reddedildi
            vm.ble.addLog("❌ Bluetooth izinleri reddedildi!")
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Yazıcı Tara", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bilgi kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Bluetooth Tarama",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Yakındaki Bluetooth LE cihazları aranacak",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "• Yazıcının açık olduğundan emin olun\n" +
                        "• Bluetooth ve konum servislerinin açık olduğunu kontrol edin\n" +
                        "• Tarama 10 saniye sürer",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Tarama butonu
                    Button(
                        onClick = { permLauncher.launch(permissions) },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Taranıyor...")
                        } else {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Taramayı Başlat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bulunan cihazlar
            if (scanResults.isNotEmpty()) {
                Text(
                    "Bulunan Cihazlar (${scanResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scanResults) { result ->
                        DeviceItem(
                            name = result.device.name ?: "Bilinmeyen Cihaz",
                            address = result.device.address,
                            rssi = result.rssi,
                            onClick = {
                                vm.connect(
                                    result.device.address,
                                    result.device.name ?: result.device.address
                                )
                                onBack()
                            }
                        )
                    }
                }
            } else if (!isScanning) {
                // Boş durum
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.BluetoothSearching,
                            null,
                            tint = Color(0xFF444466),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Henüz cihaz bulunamadı",
                            color = Color(0xFF666666),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Yukarıdaki butona basarak tarama başlatın",
                            color = Color(0xFF444444),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    name: String,
    address: String,
    rssi: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cihaz ikonu
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (name.contains("MX", true) || name.contains("print", true))
                                Accent.copy(alpha = 0.2f)
                            else
                                Color(0xFF333355)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Print,
                        null,
                        tint = if (name.contains("MX", true) || name.contains("print", true))
                            Accent
                        else
                            Color(0xFF666688),
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Cihaz bilgileri
                Column {
                    Text(
                        name,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }
            }
            
            // Sinyal gücü ve ok
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // RSSI badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        rssi > -60 -> Success.copy(alpha = 0.2f)
                        rssi > -80 -> Warning.copy(alpha = 0.2f)
                        else -> Error.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        "$rssi dBm",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            rssi > -60 -> Success
                            rssi > -80 -> Warning
                            else -> Error
                        }
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = Color(0xFF555577),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
```

---

## 🔧 İzin İsteme Süreci

### 1. Manifest'te İzinleri Tanımla
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 2. Runtime'da İzin İste
```kotlin
val permLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { results ->
    if (results.values.all { it }) {
        // Tüm izinler verildi
        vm.startScan()
    } else {
        // İzin reddedildi
        showPermissionDeniedDialog()
    }
}

// İzin iste
Button(onClick = { permLauncher.launch(permissions) }) {
    Text("Taramayı Başlat")
}
```

---

## 📋 İzin Kontrol Listesi

### Android 12+ (API 31+)
- ✅ `BLUETOOTH_SCAN` - Cihaz tarama
- ✅ `BLUETOOTH_CONNECT` - Cihaza bağlanma
- ✅ `ACCESS_FINE_LOCATION` - Konum (opsiyonel ama önerilen)

### Android 11 ve Altı (API 30-)
- ✅ `BLUETOOTH` - Temel Bluetooth
- ✅ `BLUETOOTH_ADMIN` - Bluetooth yönetimi
- ✅ `ACCESS_FINE_LOCATION` - Konum (zorunlu)

---

## 🐛 Yaygın Sorunlar ve Çözümler

### 1. "Bluetooth izinleri verilmedi" Hatası
**Çözüm:** Uygulama ayarlarından izinleri manuel olarak verin:
```
Ayarlar → Uygulamalar → MXW Printer → İzinler
```

### 2. "Tarama başarısız" Hatası
**Çözüm:**
- Bluetooth'un açık olduğundan emin olun
- Konum servislerinin açık olduğunu kontrol edin
- Uygulamayı yeniden başlatın

### 3. Cihaz Bulunamıyor
**Çözüm:**
- Yazıcının açık ve eşleşme modunda olduğundan emin olun
- Yazıcıyı yeniden başlatın
- Telefonu yazıcıya yaklaştırın (max 10 metre)

### 4. Android 12+ İzin Sorunu
**Çözüm:** `neverForLocation` flag'ini kullanın:
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
```

---

## 📊 RSSI Sinyal Gücü Değerleri

| RSSI Değeri | Sinyal Kalitesi | Mesafe (Tahmini) |
|-------------|-----------------|------------------|
| -30 dBm     | Mükemmel        | 0-1 metre        |
| -50 dBm     | Çok İyi         | 1-3 metre        |
| -60 dBm     | İyi             | 3-5 metre        |
| -70 dBm     | Orta            | 5-8 metre        |
| -80 dBm     | Zayıf           | 8-10 metre       |
| -90 dBm     | Çok Zayıf       | 10+ metre        |

---

## 🎯 Özet

1. **Manifest'e izinleri ekle**
2. **Runtime'da izin iste** (Android 6.0+)
3. **BleManager ile tara**
4. **Bulunan cihazları listele**
5. **Kullanıcı seçsin ve bağlan**

Bu yapı ile Bluetooth tarama ve bağlantı süreci sorunsuz çalışacaktır! 🚀
