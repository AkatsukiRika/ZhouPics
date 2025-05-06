package com.tgwgroup.zhoupics.utils

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_SIZE = 1024

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