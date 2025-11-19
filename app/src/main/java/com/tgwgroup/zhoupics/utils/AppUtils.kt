package com.tgwgroup.zhoupics.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.core.net.toUri
import com.tgwgroup.zhoupics.R

fun <T : Parcelable> Intent.getParcelableExtraCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, clazz)
    } else {
        this.getParcelableExtra(name) as? T
    }
}

fun Context.openUrl(url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }.onFailure {
        it.printStackTrace()
        toastError(resources.getString(R.string.unable_to_open_link))
    }
}