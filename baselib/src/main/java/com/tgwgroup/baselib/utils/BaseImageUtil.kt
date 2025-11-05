package com.tgwgroup.baselib.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 70): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}

fun Bitmap.isFullyTransparent(): Boolean {
    if (!hasAlpha()) {
        return false
    }
    val pixels = IntArray(width)
    for (y in 0 until height) {
        getPixels(pixels, 0, width, 0, y, width, 1)
        for (pixel in pixels) {
            val alpha = (pixel shr 24) and 0xFF
            if (alpha != 0) {
                return false
            }
        }
    }
    return true
}