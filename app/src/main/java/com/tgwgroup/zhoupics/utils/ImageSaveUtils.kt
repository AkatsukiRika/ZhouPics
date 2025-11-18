package com.tgwgroup.zhoupics.utils

import android.graphics.Bitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val PREFIX_CROP_RESULT = "crop_result"

const val PREFIX_ELIMINATION_RESULT = "elimination_result"

val cacheDir = File(appContext.filesDir, "cache")

suspend fun clearCache() = withContext(Dispatchers.IO) {
    if (cacheDir.exists()) {
        cacheDir.deleteRecursively()
    }
    cacheDir.mkdirs()
}

suspend fun saveBitmap(bitmap: Bitmap, prefix: String) = withContext(Dispatchers.IO) {
    try {
        val file = File(cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        }
        file.toUri()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}