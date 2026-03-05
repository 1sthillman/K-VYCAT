package com.mxw.printer.model

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

class PrinterViewModelNew(app: Application) : AndroidViewModel(app) {

    val ble = BleManager(app)

    // Connection
    val connectionState = ble.connectionState
    val scanResults = ble.scanResults
    val isScanning = ble.isScanning
    val printProgress = ble.printProgress
    val logMessages = ble.logMessages
    val lastDeviceInfo = ble.lastDeviceInfo

    // Print settings
    private val _printSettings = MutableStateFlow(PrintSettings())
    val printSettings: StateFlow<PrintSettings> = _printSettings

    // Text editor state
    private val _textContent = MutableStateFlow("Merhaba!")
    val textContent: StateFlow<String> = _textContent

    // Banner state
    private val _bannerText = MutableStateFlow("DUYURU")
    val bannerText: StateFlow<String> = _bannerText

    // Image state
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    // Preview bitmap
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap

    // Print queue
    private val _printQueue = MutableStateFlow<List<PrintJob>>(emptyList())
    val printQueue: StateFlow<List<PrintJob>> = _printQueue

    // Connected device
    private val _connectedDevice = MutableStateFlow<String?>(null)
    val connectedDevice: StateFlow<String?> = _connectedDevice

    // Show print dialog
    private val _showPrintDialog = MutableStateFlow(false)
    val showPrintDialog: StateFlow<Boolean> = _showPrintDialog

    // Show printing dialog
    private val _showPrintingDialog = MutableStateFlow(false)
    val showPrintingDialog: StateFlow<Boolean> = _showPrintingDialog

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

    fun updateSettings(settings: PrintSettings) {
        _printSettings.value = settings
        updatePreview()
    }

    fun setTextContent(v: String) {
        _textContent.value = v
        updatePreview()
    }

    fun setBannerText(v: String) {
        _bannerText.value = v
        updateBannerPreview()
    }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        updateImagePreview()
    }

    fun showPrintDialog() {
        _showPrintDialog.value = true
    }

    fun hidePrintDialog() {
        _showPrintDialog.value = false
    }

    fun updatePreview() {
        viewModelScope.launch {
            val settings = _printSettings.value
            val bmp = PrinterProtocol.textToBitmap(
                text = _textContent.value,
                fontSize = settings.fontSize,
                bold = settings.isBold,
                align = settings.textAlign,
                paperSize = settings.paperSize
            )
            val rotated = PrinterProtocol.rotateBitmap(bmp, settings.orientation)
            _previewBitmap.value = PrinterProtocol.toBinaryBitmap(rotated)
        }
    }

    fun updateBannerPreview() {
        viewModelScope.launch {
            val settings = _printSettings.value
            val bitmaps = PrinterProtocol.bannerToBitmap(
                _bannerText.value,
                settings.bannerHeight,
                settings.bannerLetterByLetter,
                settings.paperSize
            )
            val merged = if (bitmaps.size > 1) {
                PrinterProtocol.mergeBitmaps(bitmaps, settings.paperSize)
            } else {
                bitmaps.first()
            }
            val rotated = PrinterProtocol.rotateBitmap(merged, settings.orientation)
            _previewBitmap.value = PrinterProtocol.toBinaryBitmap(rotated)
        }
    }

    fun updateImagePreview() {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val stream = ctx.contentResolver.openInputStream(uri) ?: return@launch
            val src = BitmapFactory.decodeStream(stream)
            stream.close()
            val settings = _printSettings.value
            val rotated = PrinterProtocol.rotateBitmap(src, settings.orientation)
            _previewBitmap.value = PrinterProtocol.toBinaryBitmap(rotated, settings.imageThreshold)
        }
    }

    // === PRINT ACTIONS ===

    fun printText(settings: PrintSettings) {
        viewModelScope.launch {
            _showPrintingDialog.value = true
            val bmp = PrinterProtocol.textToBitmap(
                text = _textContent.value,
                fontSize = settings.fontSize,
                bold = settings.isBold,
                align = settings.textAlign,
                paperSize = settings.paperSize
            )
            val rotated = PrinterProtocol.rotateBitmap(bmp, settings.orientation)
            val binary = PrinterProtocol.toBinaryBitmap(rotated)
            ble.printWithCopies(binary, settings.heatLevel, settings.copies)
            _showPrintingDialog.value = false
        }
    }

    fun printBanner(settings: PrintSettings) {
        viewModelScope.launch {
            _showPrintingDialog.value = true
            val bitmaps = PrinterProtocol.bannerToBitmap(
                _bannerText.value,
                settings.bannerHeight,
                settings.bannerLetterByLetter,
                settings.paperSize
            )
            
            if (settings.bannerLetterByLetter) {
                // Her harf ayrı yazdır
                bitmaps.forEachIndexed { index, letterBmp ->
                    ble.addLog("Harf ${index + 1}/${bitmaps.size} yazdırılıyor...")
                    val rotated = PrinterProtocol.rotateBitmap(letterBmp, settings.orientation)
                    val binary = PrinterProtocol.toBinaryBitmap(rotated)
                    ble.print(binary, settings.heatLevel)
                }
            } else {
                val merged = bitmaps.first()
                val rotated = PrinterProtocol.rotateBitmap(merged, settings.orientation)
                val binary = PrinterProtocol.toBinaryBitmap(rotated)
                ble.printWithCopies(binary, settings.heatLevel, settings.copies)
            }
            _showPrintingDialog.value = false
        }
    }

    fun printImage(settings: PrintSettings) {
        val uri = _selectedImageUri.value ?: return
        viewModelScope.launch {
            _showPrintingDialog.value = true
            val ctx = getApplication<Application>()
            val stream = ctx.contentResolver.openInputStream(uri) ?: return@launch
            val src = BitmapFactory.decodeStream(stream)
            stream.close()
            val rotated = PrinterProtocol.rotateBitmap(src, settings.orientation)
            val binary = PrinterProtocol.toBinaryBitmap(rotated, settings.imageThreshold)
            ble.printWithCopies(binary, settings.heatLevel, settings.copies)
            _showPrintingDialog.value = false
        }
    }

    fun cancelPrint() {
        _showPrintingDialog.value = false
    }

    init {
        updatePreview()
    }
}
