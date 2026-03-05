package com.mxw.printer.model

// ============================================================
// OLD PRINTER VIEW MODEL - DISABLED
// Use PrinterViewModelNew.kt instead
// ============================================================

/*
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mxw.printer.ble.BleManager
import com.mxw.printer.ble.PrinterProtocol
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrinterViewModel(app: Application) : AndroidViewModel(app) {

    val ble = BleManager(app)

    // Connection
    val connectionState = ble.connectionState
    val scanResults = ble.scanResults
    val isScanning = ble.isScanning
    val printProgress = ble.printProgress
    val logMessages = ble.logMessages
    val lastDeviceInfo = ble.lastDeviceInfo

    // Print settings
    private val _heatLevel = MutableStateFlow(75)
    val heatLevel: StateFlow<Int> = _heatLevel

    private val _printSpeed = MutableStateFlow(50) // ms per row
    val printSpeed: StateFlow<Int> = _printSpeed

    // Text editor state
    private val _textContent = MutableStateFlow("Merhaba!")
    val textContent: StateFlow<String> = _textContent

    private val _fontSize = MutableStateFlow(0f) // 0 = auto
    val fontSize: StateFlow<Float> = _fontSize

    private val _isBold = MutableStateFlow(true)
    val isBold: StateFlow<Boolean> = _isBold

    private val _textAlign = MutableStateFlow(com.mxw.printer.model.TextAlign.CENTER)
    val textAlign: StateFlow<com.mxw.printer.model.TextAlign> = _textAlign

    // Preview bitmap
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap

    // Print queue (multiple items)
    private val _printQueue = MutableStateFlow<List<PrintItem>>(emptyList())
    val printQueue: StateFlow<List<PrintItem>> = _printQueue

    // Banner state
    private val _bannerText = MutableStateFlow("DUYURU")
    val bannerText: StateFlow<String> = _bannerText

    private val _bannerHeight = MutableStateFlow(120)
    val bannerHeight: StateFlow<Int> = _bannerHeight

    // Image state
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _imageThreshold = MutableStateFlow(128)
    val imageThreshold: StateFlow<Int> = _imageThreshold

    private val _imageDither = MutableStateFlow(false)
    val imageDither: StateFlow<Boolean> = _imageDither

    // Connected device
    private val _connectedDevice = MutableStateFlow<String?>(null)
    val connectedDevice: StateFlow<String?> = _connectedDevice

    // ===== ACTIONS =====

    fun startScan() = ble.startScan()
    fun stopScan() = ble.stopScan()
    fun autoReconnect() = ble.autoReconnect()

    fun connect(address: String, name: String) {
        _connectedDevice.value = name
        ble.connect(address, name)
    }

    fun disconnect() {
        _connectedDevice.value = null
        ble.disconnect()
    }

    fun setHeatLevel(v: Int) { _heatLevel.value = v }
    fun setPrintSpeed(v: Int) { _printSpeed.value = v }
    fun setTextContent(v: String) { _textContent.value = v; updatePreview() }
    fun setFontSize(v: Float) { _fontSize.value = v; updatePreview() }
    fun setBold(v: Boolean) { _isBold.value = v; updatePreview() }
    fun setTextAlign(v: com.mxw.printer.model.TextAlign) { _textAlign.value = v; updatePreview() }
    fun setBannerText(v: String) { _bannerText.value = v; updateBannerPreview() }
    fun setBannerHeight(v: Int) { _bannerHeight.value = v; updateBannerPreview() }
    fun setImageThreshold(v: Int) { _imageThreshold.value = v; updateImagePreview() }
    fun setImageDither(v: Boolean) { _imageDither.value = v; updateImagePreview() }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        updateImagePreview()
    }

    fun updatePreview() {
        val bmp = PrinterProtocol.textToBitmap(
            text = _textContent.value,
            fontSize = _fontSize.value,
            bold = _isBold.value,
            align = _textAlign.value
        )
        _previewBitmap.value = PrinterProtocol.toBinaryBitmap(bmp)
    }

    fun updateBannerPreview() {
        val bmp = PrinterProtocol.bannerToBitmap(_bannerText.value, _bannerHeight.value)
        _previewBitmap.value = PrinterProtocol.toBinaryBitmap(bmp)
    }

    fun updateImagePreview() {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val stream = ctx.contentResolver.openInputStream(uri) ?: return@launch
            val src = BitmapFactory.decodeStream(stream)
            stream.close()
            _previewBitmap.value = PrinterProtocol.toBinaryBitmap(src, _imageThreshold.value)
        }
    }

    // Print queue management
    fun addToQueue(item: PrintItem) {
        _printQueue.value = _printQueue.value + item
    }

    fun removeFromQueue(index: Int) {
        val list = _printQueue.value.toMutableList()
        if (index in list.indices) list.removeAt(index)
        _printQueue.value = list
    }

    fun clearQueue() { _printQueue.value = emptyList() }

    fun moveQueueItem(from: Int, to: Int) {
        val list = _printQueue.value.toMutableList()
        if (from in list.indices && to in list.indices) {
            val item = list.removeAt(from)
            list.add(to, item)
            _printQueue.value = list
        }
    }

    // === PRINT ACTIONS ===

    fun printText() {
        val bmp = PrinterProtocol.textToBitmap(
            text = _textContent.value,
            fontSize = _fontSize.value,
            bold = _isBold.value,
            align = _textAlign.value
        )
        printBitmap(bmp)
    }

    fun printBanner() {
        val bmp = PrinterProtocol.bannerToBitmap(_bannerText.value, _bannerHeight.value)
        printBitmap(bmp)
    }

    fun printImage() {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val stream = ctx.contentResolver.openInputStream(uri) ?: return@launch
            val src = BitmapFactory.decodeStream(stream)
            stream.close()
            val bmp = PrinterProtocol.toBinaryBitmap(src, _imageThreshold.value)
            ble.print(bmp, _heatLevel.value)
        }
    }

    fun printQueue() {
        viewModelScope.launch {
            val bitmaps = _printQueue.value.map { item ->
                when (item) {
                    is PrintItem.Text    -> PrinterProtocol.textToBitmap(item.text, item.fontSize, item.bold, item.align)
                    is PrintItem.Banner  -> PrinterProtocol.bannerToBitmap(item.text, item.height)
                    is PrintItem.Divider -> PrinterProtocol.dividerToBitmap(item.style)
                    is PrintItem.Spacer  -> PrinterProtocol.spacerBitmap(item.lines)
                    is PrintItem.Image   -> {
                        val ctx = getApplication<Application>()
                        val stream = ctx.contentResolver.openInputStream(item.uri) ?: return@map PrinterProtocol.spacerBitmap(1)
                        val src = BitmapFactory.decodeStream(stream)
                        stream.close()
                        PrinterProtocol.toBinaryBitmap(src, item.threshold)
                    }
                }
            }
            val merged = PrinterProtocol.mergeBitmaps(bitmaps)
            ble.print(merged, _heatLevel.value)
        }
    }

    fun printBitmap(bmp: Bitmap) {
        viewModelScope.launch {
            ble.print(bmp, _heatLevel.value)
        }
    }

    fun printDivider(style: PrinterProtocol.DividerStyle) {
        printBitmap(PrinterProtocol.dividerToBitmap(style))
    }

    fun printSpacer(lines: Int = 3) {
        printBitmap(PrinterProtocol.spacerBitmap(lines))
    }

    init { updatePreview() }
}

sealed class PrintItem {
    data class Text(
        val text: String,
        val fontSize: Float = 0f,
        val bold: Boolean = true,
        val align: com.mxw.printer.model.TextAlign = com.mxw.printer.model.TextAlign.CENTER
    ) : PrintItem()

    data class Banner(val text: String, val height: Int = 120) : PrintItem()
    data class Divider(val style: PrinterProtocol.DividerStyle = PrinterProtocol.DividerStyle.SOLID) : PrintItem()
    data class Spacer(val lines: Int = 1) : PrintItem()
    data class Image(val uri: android.net.Uri, val threshold: Int = 128) : PrintItem()
}

}
*/
