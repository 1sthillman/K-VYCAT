package com.mxw.printer.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.*
import java.util.UUID

/**
 * BASIT BLE MANAGER - Python printer_app.py mantığı
 * Cat-Printer'dan esinlenildi
 */
@SuppressLint("MissingPermission")
class BleManagerSimple(private val context: Context) {

    private val TAG = "BleManagerSimple"
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private var gatt: BluetoothGatt? = null
    
    private var cmdChar: BluetoothGattCharacteristic? = null
    private var dataChar: BluetoothGattCharacteristic? = null
    
    private val CMD_UUID = UUID.fromString("0000ae01-0000-1000-8000-00805f9b34fb")
    private val DATA_UUID = UUID.fromString("0000ae03-0000-1000-8000-00805f9b34fb")
    
    private val MTU = 200  // Cat-Printer MTU
    
    var onProgress: ((Float) -> Unit)? = null
    var onLog: ((String) -> Unit)? = null
    
    private fun log(msg: String) {
        Log.d(TAG, msg)
        onLog?.invoke(msg)
    }
    
    // Connect
    suspend fun connect(address: String): Boolean = suspendCancellableCoroutine { continuation ->
        log("Bağlanıyor: $address")
        
        val device = adapter.getRemoteDevice(address)
        
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        log("Bağlandı! Servisler keşfediliyor...")
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        log("Bağlantı kesildi")
                        if (continuation.isActive) {
                            continuation.resume(false) {}
                        }
                    }
                }
            }
            
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Find characteristics
                    gatt.services.forEach { service ->
                        service.characteristics.forEach { char ->
                            when (char.uuid) {
                                CMD_UUID -> {
                                    cmdChar = char
                                    log("CMD bulundu")
                                }
                                DATA_UUID -> {
                                    dataChar = char
                                    log("DATA bulundu")
                                }
                            }
                        }
                    }
                    
                    if (cmdChar != null && dataChar != null) {
                        log("✅ Yazıcı hazır!")
                        if (continuation.isActive) {
                            continuation.resume(true) {}
                        }
                    } else {
                        log("HATA: Karakteristikler bulunamadı!")
                        if (continuation.isActive) {
                            continuation.resume(false) {}
                        }
                    }
                }
            }
        }
        
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
    }
    
    // Disconnect
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        cmdChar = null
        dataChar = null
        log("Bağlantı kesildi")
    }
    
    // Print - Python printer_app.py ile AYNI
    suspend fun print(bitmap: Bitmap) {
        if (gatt == null || cmdChar == null || dataChar == null) {
            log("HATA: Bağlı değil!")
            return
        }
        
        log("Yazdırma başlıyor...")
        onProgress?.invoke(0f)
        
        // Encode - Python ile AYNI
        val encoded = PrinterProtocol.encodeBitmap(bitmap)
        val rows = bitmap.height
        
        // Commands - Python ile AYNI
        log("Komutlar gönderiliyor...")
        val commands = listOf(
            "2221A70000000000" to 500L,
            "2221B10001000000FF" to 500L,
            "2221A10001000000FF" to 500L,
            "2221A2000100FFFFFF" to 1000L,
            "2221A9000400000230000000" to 1000L
        )
        
        for ((hex, delayMs) in commands) {
            writeChar(cmdChar!!, hexToBytes(hex))
            delay(delayMs)
        }
        
        log("$rows satır gönderiliyor...")
        
        // Data rows - Python ile AYNI
        for (i in 0 until rows) {
            val row = encoded.sliceArray(i * 48 until (i + 1) * 48)
            writeChar(dataChar!!, row)
            delay(50)  // Python: 0.05s
            
            onProgress?.invoke((i + 1).toFloat() / rows)
            
            if ((i + 1) % 50 == 0) {
                log("İlerleme: ${i + 1}/$rows")
            }
        }
        
        // End - Python ile AYNI
        log("Bitiriliyor...")
        writeChar(cmdChar!!, hexToBytes("2221AD000100000000"))
        delay(2000)
        
        onProgress?.invoke(1f)
        log("✅ Yazdırma tamamlandı!")
    }
    
    // Write characteristic - Cat-Printer mantığı
    private suspend fun writeChar(char: BluetoothGattCharacteristic, data: ByteArray) {
        withContext(Dispatchers.Main) {
            // NO_RESPONSE kullan - Python: response=False
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                gatt?.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            } else {
                char.value = data
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                gatt?.writeCharacteristic(char)
            }
        }
        // Cat-Printer: 20ms delay
        delay(20)
    }
    
    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
