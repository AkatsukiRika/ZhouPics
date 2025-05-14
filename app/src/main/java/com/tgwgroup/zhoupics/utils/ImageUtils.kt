package com.tgwgroup.zhoupics.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.tgwgroup.baselib.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ImageUtils"

const val MAX_SIZE = 1024

suspend fun getBitmap(model: Any, overrideSize: Int = MAX_SIZE): Bitmap? = withContext(Dispatchers.IO) {
    Glide
        .with(appContext)
        .asBitmap()
        .load(model)
        .priority(Priority.IMMEDIATE)
        .override(overrideSize)
        .submit()
        .get()
}

fun scaleByLongEdge(bitmap: Bitmap, targetSize: Int): Bitmap {
    val srcW = bitmap.width
    val srcH = bitmap.height

    var dstBitmap: Bitmap? = null
    runCatching {
        val isWidthLonger = srcW >= srcH

        val scale = if (isWidthLonger) {
            targetSize.toFloat() / srcW
        } else {
            targetSize.toFloat() / srcH
        }

        val matrix = Matrix()
        matrix.postScale(scale, scale)
        dstBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, srcW, srcH, matrix,
            true
        )
        LogUtil.d(TAG, "scaleByLongSide srcW=$srcW, srcH=$srcH, targetWidth=${dstBitmap?.width}, targetHeight=${dstBitmap?.height}")
    }
    return dstBitmap ?: bitmap
}