package com.mxw.printer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val TAG = "BleManager"
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private var gatt: BluetoothGatt? = null
    private var scanner: BluetoothLeScanner? = null
    
    // SharedPreferences for saving last device
    private val prefs = context.getSharedPreferences("mxw_printer_prefs", Context.MODE_PRIVATE)
    private val PREF_LAST_DEVICE_ADDRESS = "last_device_address"
    private val PREF_LAST_DEVICE_NAME = "last_device_name"

    // State flows
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults

    private val _printProgress = MutableStateFlow(0f)
    val printProgress: StateFlow<Float> = _printProgress

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    private val _lastDeviceInfo = MutableStateFlow<Pair<String, String>?>(null) // (address, name)
    val lastDeviceInfo: StateFlow<Pair<String, String>?> = _lastDeviceInfo

    private var cmdChar: BluetoothGattCharacteristic? = null
    private var dataChar: BluetoothGattCharacteristic? = null
    private var currentDeviceAddress: String? = null
    
    // Write synchronization - Python'daki await için
    private val writeMutex = kotlinx.coroutines.sync.Mutex()
    private var writeResult: CompletableDeferred<Boolean>? = null
    
    init {
        // Load last connected device
        val lastAddress = prefs.getString(PREF_LAST_DEVICE_ADDRESS, null)
        val lastName = prefs.getString(PREF_LAST_DEVICE_NAME, null)
        if (lastAddress != null && lastName != null) {
            _lastDeviceInfo.value = Pair(lastAddress, lastName)
            addLog("Son bağlanan cihaz: $lastName ($lastAddress)")
        }
    }
    
    // Save last connected device
    private fun saveLastDevice(address: String, name: String) {
        prefs.edit().apply {
            putString(PREF_LAST_DEVICE_ADDRESS, address)
            putString(PREF_LAST_DEVICE_NAME, name)
            apply()
        }
        _lastDeviceInfo.value = Pair(address, name)
        addLog("Cihaz kaydedildi: $name")
    }
    
    // Auto reconnect to last device
    fun autoReconnect() {
        val lastDevice = _lastDeviceInfo.value
        if (lastDevice != null) {
            addLog("Otomatik bağlanıyor: ${lastDevice.second}")
            connect(lastDevice.first, lastDevice.second)
        } else {
            addLog("Kaydedilmiş cihaz yok")
        }
    }

    fun addLog(msg: String) {
        Log.d(TAG, msg)
        val current = _logMessages.value.toMutableList()
        current.add("[${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}] $msg")
        if (current.size > 200) current.removeAt(0)
        _logMessages.value = current
    }

    // === SCAN ===
    fun startScan() {
        val results = mutableListOf<ScanResult>()
        _scanResults.value = emptyList()
        _isScanning.value = true
        scanner = adapter.bluetoothLeScanner
        addLog("Tarama başlatıldı...")

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device.name != null && !results.any { it.device.address == result.device.address }) {
                    results.add(result)
                    _scanResults.value = results.toList()
                    addLog("Cihaz: ${result.device.name} (${result.device.address}) RSSI:${result.rssi}")
                }
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(null, settings, callback)

        CoroutineScope(Dispatchers.IO).launch {
            delay(10000)
            scanner?.stopScan(callback)
            _isScanning.value = false
            addLog("Tarama bitti. ${results.size} cihaz bulundu.")
        }
    }

    fun stopScan() {
        _isScanning.value = false
    }

    // === CONNECT ===
    fun connect(address: String, name: String = "MXW01") {
        currentDeviceAddress = address
        _connectionState.value = ConnectionState.CONNECTING
        addLog("Bağlanıyor: $name ($address)")
        val device = adapter.getRemoteDevice(address)

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        addLog("Bağlandı! Servisler keşfediliyor...")
                        _connectionState.value = ConnectionState.DISCOVERING
                        gatt.discoverServices()
                        // Save device for auto-reconnect
                        saveLastDevice(address, name)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        addLog("Bağlantı kesildi.")
                        _connectionState.value = ConnectionState.DISCONNECTED
                        cmdChar = null; dataChar = null
                        
                        // Auto-reconnect if unexpected disconnect
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            addLog("Beklenmeyen bağlantı kesintisi. 3 saniye sonra yeniden bağlanılacak...")
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(3000)
                                if (_connectionState.value == ConnectionState.DISCONNECTED) {
                                    addLog("Yeniden bağlanıyor...")
                                    connect(address, name)
                                }
                            }
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.services.forEach { service ->
                        service.characteristics.forEach { char ->
                            val uuid = char.uuid.toString()
                            if (uuid == PrinterProtocol.CMD_UUID) {
                                cmdChar = char
                                addLog("CMD karakteristiği bulundu")
                            }
                            if (uuid == PrinterProtocol.DATA_UUID) {
                                dataChar = char
                                addLog("DATA karakteristiği bulundu")
                            }
                        }
                    }
                    if (cmdChar != null && dataChar != null) {
                        _connectionState.value = ConnectionState.CONNECTED
                        addLog("✅ Yazıcı hazır!")
                    } else {
                        addLog("HATA: Yazıcı karakteristikleri bulunamadı!")
                        _connectionState.value = ConnectionState.ERROR
                    }
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, char: BluetoothGattCharacteristic, status: Int) {
                // Write tamamlandı
                addLog("onCharacteristicWrite: status=$status")
                writeResult?.complete(status == BluetoothGatt.GATT_SUCCESS)
            }
        }

        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        cmdChar = null; dataChar = null
        addLog("Bağlantı kesildi.")
    }

    // === PRINT - Python kodundan BIREBIR ===
    suspend fun print(bitmap: Bitmap, heatLevel: Int = 75) {
        // Bağlantı kontrolü
        if (gatt == null) {
            addLog("HATA: GATT bağlantısı yok!")
            return
        }
        
        if (_connectionState.value != ConnectionState.CONNECTED) {
            addLog("HATA: Yazıcı bağlı değil! Durum: ${_connectionState.value}")
            return
        }
        
        val cmd = cmdChar ?: run { addLog("HATA: CMD karakteristiği yok!"); return }
        val data = dataChar ?: run { addLog("HATA: DATA karakteristiği yok!"); return }

        addLog("Yazdırma başlıyor...")
        addLog("GATT: ${gatt != null}, CMD: ${cmd != null}, DATA: ${data != null}")
        _printProgress.value = 0f

        // Python'daki AYNI encoding
        val encoded = withContext(Dispatchers.Default) {
            PrinterProtocol.encodeBitmap(bitmap)
        }
        val rows = bitmap.height

        // Python'daki AYNI komut sırası ve delay'ler
        addLog("Komutlar gönderiliyor...")
        
        // CMD_START - write ve bekle, sonra 0.5s delay
        if (!writeCmd(cmd, PrinterProtocol.CMD_START)) {
            addLog("HATA: CMD_START gönderilemedi!")
            return
        }
        delay(500)
        
        // CMD_CONFIG1 - write ve bekle, sonra 0.5s delay
        if (!writeCmd(cmd, PrinterProtocol.CMD_CONFIG1)) {
            addLog("HATA: CMD_CONFIG1 gönderilemedi!")
            return
        }
        delay(500)
        
        // CMD_CONFIG2 - write ve bekle, sonra 0.5s delay
        if (!writeCmd(cmd, PrinterProtocol.CMD_CONFIG2)) {
            addLog("HATA: CMD_CONFIG2 gönderilemedi!")
            return
        }
        delay(500)
        
        // CMD_HEAT - write ve bekle, sonra 1.0s delay
        if (!writeCmd(cmd, PrinterProtocol.cmdHeat(heatLevel))) {
            addLog("HATA: CMD_HEAT gönderilemedi!")
            return
        }
        delay(1000)
        
        // CMD_HEADER - write ve bekle, sonra 1.0s delay
        if (!writeCmd(cmd, PrinterProtocol.cmdHeader())) {
            addLog("HATA: CMD_HEADER gönderilemedi!")
            return
        }
        delay(1000)

        addLog("$rows satır gönderiliyor...")

        // Satır verilerini gönder - ÇOK YAVAŞ ama SIRASIZ OLMASIN
        for (i in 0 until rows) {
            val row = encoded.slice(i * PrinterProtocol.BYTES_PER_ROW until (i + 1) * PrinterProtocol.BYTES_PER_ROW).toByteArray()
            
            // Progress'i ÖNCE güncelle (kullanıcı görsün)
            _printProgress.value = (i + 1).toFloat() / rows
            
            // Write (MAIN THREAD'de, sıralı)
            if (!writeCmd(data, row)) {
                addLog("HATA: Satır $i gönderilemedi!")
                return
            }
            
            // Her 10 satırda bir log
            if ((i + 1) % 10 == 0) {
                addLog("Gönderildi: ${i + 1}/$rows satır")
            }
        }

        // Baskı bitir - write ve bekle, sonra 2.0s delay
        addLog("Bitiriliyor...")
        if (!writeCmd(cmd, PrinterProtocol.CMD_END)) {
            addLog("HATA: CMD_END gönderilemedi!")
            return
        }
        delay(2000)
        
        _printProgress.value = 1f
        addLog("✅ Yazdırma tamamlandı!")
    }
    
    // === PRINT WITH COPIES ===
    suspend fun printWithCopies(bitmap: Bitmap, heatLevel: Int = 75, copies: Int = 1) {
        repeat(copies) { copy ->
            addLog("Kopya ${copy + 1}/$copies yazdırılıyor...")
            print(bitmap, heatLevel)
            if (copy < copies - 1) {
                delay(2000) // Kopyalar arası bekleme
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun writeCmd(char: BluetoothGattCharacteristic, data: ByteArray): Boolean {
        // MUTEX ile sıralı yazma - aynı anda sadece 1 write
        writeMutex.lock()
        try {
            val currentGatt = gatt
            if (currentGatt == null) {
                addLog("HATA: GATT null!")
                return false
            }
            
            // CompletableDeferred oluştur
            writeResult = CompletableDeferred()
            
            // WRITE_TYPE_DEFAULT kullan - callback bekle
            val started = withContext(Dispatchers.Main) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    try {
                        currentGatt.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) == BluetoothGatt.GATT_SUCCESS
                    } catch (e: Exception) {
                        addLog("Write exception: ${e.message}")
                        false
                    }
                } else {
                    char.value = data
                    char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    currentGatt.writeCharacteristic(char)
                }
            }
            
            if (!started) {
                addLog("HATA: writeCharacteristic false döndü!")
                writeResult = null
                return false
            }
            
            // Callback'i bekle - Python'daki await gibi
            val success = try {
                withTimeout(3000) {
                    writeResult?.await() ?: false
                }
            } catch (e: TimeoutCancellationException) {
                addLog("HATA: Write timeout!")
                false
            }
            
            writeResult = null
            
            // Küçük delay - Bluetooth stack için
            delay(50)
            
            return success
            
        } finally {
            writeMutex.unlock()
        }
    }

    enum class ConnectionState {
        DISCONNECTED, SCANNING, CONNECTING, DISCOVERING, CONNECTED, PRINTING, ERROR
    }
}
