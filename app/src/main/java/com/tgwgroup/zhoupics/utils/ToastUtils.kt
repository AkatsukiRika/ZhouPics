package com.tgwgroup.zhoupics.utils

import android.widget.Toast
import es.dmoral.toasty.Toasty

fun toastSuccess(message: String) {
    Toasty.success(appContext, message, Toast.LENGTH_SHORT, true).show()
}

fun toastError(message: String) {
    Toasty.error(appContext, message, Toast.LENGTH_SHORT, true).show()
}