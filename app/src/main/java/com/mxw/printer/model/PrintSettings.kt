package com.mxw.printer.model

import android.graphics.Bitmap

// Paper sizes
enum class PaperSize(val widthMm: Int, val heightMm: Int?, val widthPx: Int, val displayName: String) {
    SIZE_58MM(58, null, 384, "58mm (Sürekli)"),
    SIZE_58x40MM(58, 40, 384, "58mm × 40mm"),
    SIZE_58x50MM(58, 50, 384, "58mm × 50mm"),
    SIZE_58x60MM(58, 60, 384, "58mm × 60mm"),
    SIZE_58x80MM(58, 80, 384, "58mm × 80mm"),
    SIZE_58x86MM(58, 86, 384, "58mm × 86mm"),
    SIZE_80MM(80, null, 576, "80mm (Sürekli)"),
    SIZE_80x60MM(80, 60, 576, "80mm × 60mm"),
    SIZE_80x80MM(80, 80, 576, "80mm × 80mm"),
    SIZE_80x120MM(80, 120, 576, "80mm × 120mm");
    
    fun getHeightPx(): Int? {
        return heightMm?.let { (it * widthPx) / widthMm }
    }
    
    fun isContinuous(): Boolean = heightMm == null
}

// Print orientation
enum class PrintOrientation {
    PORTRAIT,  // Dikey
    LANDSCAPE  // Yatay
}

// Print settings
data class PrintSettings(
    val paperSize: PaperSize = PaperSize.SIZE_58MM,
    val orientation: PrintOrientation = PrintOrientation.PORTRAIT,
    val heatLevel: Int = 75,
    val copies: Int = 1,
    val fontSize: Float = 0f, // 0 = auto
    val isBold: Boolean = true,
    val textAlign: TextAlign = TextAlign.CENTER,
    val imageThreshold: Int = 128,
    val bannerHeight: Int = 120,
    val bannerLetterByLetter: Boolean = false // Harf harf büyük çıktı
)

enum class TextAlign {
    LEFT, CENTER, RIGHT
}

// Print job for queue
data class PrintJob(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: PrintJobType,
    val content: Any, // String for text, Uri for image, etc.
    val settings: PrintSettings,
    val preview: Bitmap? = null
)

enum class PrintJobType {
    TEXT,
    BANNER,
    IMAGE,
    DIVIDER,
    SPACER
}

// Paper consumption calculation
data class PaperConsumption(
    val heightMm: Int,
    val heightPx: Int,
    val copies: Int
) {
    fun getTotalMm(): Int = heightMm * copies
    fun getTotalCm(): Float = getTotalMm() / 10f
}
