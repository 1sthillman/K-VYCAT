package com.mxw.printer.ble

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Matrix
import com.mxw.printer.model.PaperSize
import com.mxw.printer.model.PrintOrientation
import com.mxw.printer.model.TextAlign

object PrinterProtocol {

    const val PRINTER_WIDTH_PX = 384
    const val BYTES_PER_ROW = 48 // 384 / 8

    val CMD_UUID = "0000ae01-0000-1000-8000-00805f9b34fb"
    val DATA_UUID = "0000ae03-0000-1000-8000-00805f9b34fb"

    // === KOMUTLAR ===
    val CMD_START   = hexToBytes("2221A70000000000")
    val CMD_CONFIG1 = hexToBytes("2221B10001000000FF")
    val CMD_CONFIG2 = hexToBytes("2221A10001000000FF")
    val CMD_END     = hexToBytes("2221AD000100000000")

    fun cmdHeat(level: Int): ByteArray {
        // Basit heat komutu - sabit format
        return hexToBytes("2221A2000100FFFFFF")
    }
    
    fun cmdHeader(): ByteArray {
        return hexToBytes("2221A9000400000230000000")
    }

    // === BİTMAP ENCODER (LSB First) - Python kodundan BIREBIR ===
    fun encodeBitmap(bitmap: Bitmap): ByteArray {
        // Python: bw = img.convert('L').point(lambda p: 0 if p < 128 else 255, '1')
        val bw = toBinaryBitmap(bitmap, threshold = 128)
        val data = mutableListOf<Byte>()
        
        // Python ile AYNI algoritma
        for (y in 0 until bw.height) {
            for (x in 0 until PRINTER_WIDTH_PX step 8) {
                var byte = 0
                for (bit in 0 until 8) {
                    val px = x + bit
                    // Python: if x + bit < bw.width and bw.getpixel((x + bit, y)) == 0:
                    //         byte |= (1 << bit)
                    if (px < bw.width && bw.getPixel(px, y) == Color.BLACK) {
                        byte = byte or (1 shl bit)  // LSB first - Python ile AYNI
                    }
                }
                data.add(byte.toByte())
            }
        }
        return data.toByteArray()
    }

    // === GÖRÜNTÜ HAZIRLAMA ===
    fun toBinaryBitmap(src: Bitmap, threshold: Int = 128): Bitmap {
        val scaled = if (src.width != PRINTER_WIDTH_PX) {
            val ratio = PRINTER_WIDTH_PX.toFloat() / src.width
            val newH = (src.height * ratio).toInt()
            Bitmap.createScaledBitmap(src, PRINTER_WIDTH_PX, newH, true)
        } else src

        val result = Bitmap.createBitmap(PRINTER_WIDTH_PX, scaled.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(scaled.width * scaled.height)
        scaled.getPixels(pixels, 0, scaled.width, 0, 0, scaled.width, scaled.height)
        
        for (i in pixels.indices) {
            val r = Color.red(pixels[i])
            val g = Color.green(pixels[i])
            val b = Color.blue(pixels[i])
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = if (gray < threshold) Color.BLACK else Color.WHITE
        }
        
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    // === ROTATE BITMAP ===
    fun rotateBitmap(bitmap: Bitmap, orientation: PrintOrientation): Bitmap {
        return when (orientation) {
            PrintOrientation.PORTRAIT -> bitmap
            PrintOrientation.LANDSCAPE -> {
                val matrix = Matrix().apply { postRotate(90f) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
    }

    // === METİN BİTMAP ===
    fun textToBitmap(
        text: String,
        fontSize: Float = 0f,
        bold: Boolean = true,
        align: TextAlign = TextAlign.CENTER,
        lineSpacing: Float = 1.2f,
        maxLines: Int = 1,
        paperSize: PaperSize = PaperSize.SIZE_58MM
    ): Bitmap {
        val typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            this.typeface = typeface
        }

        val lines = text.split("\n").take(maxLines)
        val maxTextWidth = paperSize.widthPx - 8f

        val fs = if (fontSize <= 0f) {
            var size = 120f
            lines.forEach { line ->
                paint.textSize = size
                while (paint.measureText(line) > maxTextWidth && size > 10f) {
                    size -= 1f
                    paint.textSize = size
                }
            }
            size
        } else fontSize

        paint.textSize = fs

        val lineHeight = (fs * lineSpacing).toInt()
        val totalHeight = lineHeight * lines.size + 24
        
        val bitmap = Bitmap.createBitmap(paperSize.widthPx, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        lines.forEachIndexed { i, line ->
            val textWidth = paint.measureText(line)
            val x = when (align) {
                TextAlign.LEFT   -> 4f
                TextAlign.CENTER -> (paperSize.widthPx - textWidth) / 2f
                TextAlign.RIGHT  -> paperSize.widthPx - textWidth - 4f
            }
            val y = (i + 1) * lineHeight.toFloat() + 10f
            canvas.drawText(line, x, y, paint)
        }
        
        return bitmap
    }

    // === BANNER MOD (Harf-harf büyük) ===
    fun bannerToBitmap(
        text: String,
        height: Int = 200,
        letterByLetter: Boolean = false,
        paperSize: PaperSize = PaperSize.SIZE_58MM
    ): List<Bitmap> {
        if (letterByLetter) {
            // Her harf ayrı bitmap
            return text.map { char ->
                createLetterBitmap(char.toString(), paperSize)
            }
        } else {
            // Tek bitmap
            return listOf(createBannerBitmap(text, height, paperSize))
        }
    }

    private fun createLetterBitmap(letter: String, paperSize: PaperSize): Bitmap {
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        
        // Tam sayfa kullan
        val targetHeight = paperSize.getHeightPx() ?: paperSize.widthPx
        paint.textSize = targetHeight * 0.8f
        
        // Genişliğe sığdır
        while (paint.measureText(letter) > paperSize.widthPx - 8f && paint.textSize > 10f) {
            paint.textSize -= 1f
        }
        
        val bitmap = Bitmap.createBitmap(paperSize.widthPx, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val x = (paperSize.widthPx - paint.measureText(letter)) / 2f
        val y = targetHeight * 0.7f
        canvas.drawText(letter, x, y, paint)
        
        return bitmap
    }

    private fun createBannerBitmap(text: String, height: Int, paperSize: PaperSize): Bitmap {
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        
        paint.textSize = height * 0.85f
        while (paint.measureText(text) > paperSize.widthPx - 8f && paint.textSize > 10f) {
            paint.textSize -= 1f
        }
        
        val bitmap = Bitmap.createBitmap(paperSize.widthPx, height + 20, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val x = (paperSize.widthPx - paint.measureText(text)) / 2f
        canvas.drawText(text, x, height.toFloat(), paint)
        return bitmap
    }

    // === DIVIDER ===
    fun dividerToBitmap(style: DividerStyle = DividerStyle.SOLID, paperSize: PaperSize = PaperSize.SIZE_58MM): Bitmap {
        val bitmap = Bitmap.createBitmap(paperSize.widthPx, 16, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply { color = Color.BLACK; strokeWidth = 3f }
        when (style) {
            DividerStyle.SOLID  -> canvas.drawLine(4f, 8f, paperSize.widthPx - 4f, 8f, paint)
            DividerStyle.DASHED -> {
                var x = 4f
                while (x < paperSize.widthPx - 4f) {
                    canvas.drawLine(x, 8f, (x + 12f).coerceAtMost(paperSize.widthPx - 4f), 8f, paint)
                    x += 18f
                }
            }
            DividerStyle.DOUBLE -> {
                canvas.drawLine(4f, 5f, paperSize.widthPx - 4f, 5f, paint)
                canvas.drawLine(4f, 11f, paperSize.widthPx - 4f, 11f, paint)
            }
        }
        return bitmap
    }

    // === BOŞLUK ===
    fun spacerBitmap(lines: Int = 1, paperSize: PaperSize = PaperSize.SIZE_58MM): Bitmap {
        val bitmap = Bitmap.createBitmap(paperSize.widthPx, 24 * lines, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        return bitmap
    }

    // === BİTMAP BİRLEŞTİR ===
    fun mergeBitmaps(bitmaps: List<Bitmap>, paperSize: PaperSize = PaperSize.SIZE_58MM): Bitmap {
        val totalHeight = bitmaps.sumOf { it.height }
        val result = Bitmap.createBitmap(paperSize.widthPx, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        var y = 0
        bitmaps.forEach { bmp ->
            canvas.drawBitmap(bmp, 0f, y.toFloat(), null)
            y += bmp.height
        }
        return result
    }

    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    enum class DividerStyle { SOLID, DASHED, DOUBLE }
}
