# Cat-Printer Bluetooth Mantığı

## Kritik Farklar

### 1. Buffer + Flush Sistemi
```python
# Cat-Printer
self._pending_data = io.BytesIO()

def send(self, data):
    self._pending_data.write(data)
    if self._pending_data.tell() > self.mtu * 16:
        self.flush()

def flush(self):
    while chunk := self._pending_data.read(self.mtu):
        self.loop(
            self.device.write_gatt_char(self.tx_characteristic, chunk),
            asyncio.sleep(0.02)  # 20ms delay
        )
```

### 2. MTU Boyutunda Chunk'lar
- MTU = 200 byte
- Her chunk sonrası 20ms delay
- Buffer dolunca otomatik flush

### 3. NO_RESPONSE Kullanımı
- `response=False` (Python Bleak)
- Ama buffer + delay ile sıralı gönderim

## Kotlin İmplementasyonu

```kotlin
private suspend fun flushBuffer(char: BluetoothGattCharacteristic, buffer: List<ByteArray>) {
    val MTU = 200
    val allData = buffer.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
    
    var offset = 0
    while (offset < allData.size) {
        val chunkSize = minOf(MTU, allData.size - offset)
        val chunk = allData.sliceArray(offset until offset + chunkSize)
        
        withContext(Dispatchers.Main) {
            char.value = chunk
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            gatt?.writeCharacteristic(char)
        }
        
        delay(20)  // Cat-Printer: 20ms
        offset += chunkSize
    }
}
```

## Yazdırma Akışı

1. Komutları buffer'a ekle
2. Flush (MTU chunk'lar + 20ms delay)
3. 2 saniye bekle
4. Satırları 10'ar 10'ar buffer'a ekle
5. Her 10 satırda flush + 200ms delay
6. Kalan satırları flush
7. END komutu flush
8. 2 saniye bekle

Bu mantık Cat-Printer'da ÇALIŞIYOR!
