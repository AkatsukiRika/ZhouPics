package com.tgwgroup.baselib.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 70): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}