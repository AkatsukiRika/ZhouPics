package com.tgwgroup.zhoupics.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.PreferredColorSpace
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.tgwgroup.baselib.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.sqrt

private const val TAG = "ImageUtils"

var maxSize = 1024

fun getMaxTotalPixels() = maxSize * maxSize

fun initMaxSizeByDeviceLevel() {
    val deviceLevel = getDeviceLevel(appContext)
    maxSize = when (deviceLevel) {
        DEVICE_LEVEL_LOW, DEVICE_LEVEL_MID -> 1024
        DEVICE_LEVEL_HIGH -> 1440
        DEVICE_LEVEL_ULTRA -> 2048
        else -> 1024
    }
}

suspend fun getBitmap(model: Any, maxPixels: Int = getMaxTotalPixels()): Bitmap? = withContext(Dispatchers.IO) {
    val originalBitmap = Glide
        .with(appContext)
        .asBitmap()
        .load(model)
        .set(Downsampler.PREFERRED_COLOR_SPACE, PreferredColorSpace.SRGB)
        .priority(Priority.IMMEDIATE)
        .submit()
        .get()
    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height
    val totalPixels = originalWidth * originalHeight
    if (totalPixels <= maxPixels) {
        return@withContext originalBitmap
    }
    return@withContext scaleByTotalPixels(originalBitmap, maxPixels)
}

fun ImageView.loadImage(model: Any) {
    val request = Glide.with(context).load(model)
    request.into(this)
}

fun scaleByTotalPixels(bitmap: Bitmap, maxPixels: Int): Bitmap {
    val srcW = bitmap.width
    val srcH = bitmap.height

    val currentPixels = srcW * srcH

    if (currentPixels <= maxPixels) {
        return bitmap
    }

    val scale = sqrt(maxPixels / currentPixels.toFloat())

    val matrix = Matrix().apply {
        postScale(scale, scale)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, srcW, srcH, matrix, true).also {
        LogUtil.d(TAG, "scaleByTotalPixels srcW=$srcW, srcH=$srcH, scaledW=${it.width}, scaledH=${it.height}")
    }
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

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): Uri? {
    val contentResolver = context.contentResolver

    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val imageUri = contentResolver.insert(imageCollection, contentValues)

    imageUri?.let { uri ->
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            if (!bitmap.compress(CompressFormat.JPEG, 95, outputStream)) {
                throw IOException("Failed to save bitmap.")
            }
        } ?: throw IOException("Failed to open output stream.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }
    }

    return imageUri
}