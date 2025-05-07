package com.tgwgroup.baselib.utils

import android.util.Log

object LogUtil {
    fun v(tag: String, msg: String, e: Throwable? = null) {
        Log.v(tag, msg, e)
    }

    fun d(tag: String, msg: String, e: Throwable? = null) {
        Log.d(tag, msg, e)
    }

    fun i(tag: String, msg: String, e: Throwable? = null) {
        Log.i(tag, msg, e)
    }

    fun w(tag: String, msg: String, e: Throwable? = null) {
        Log.w(tag, msg, e)
    }

    fun e(tag: String, msg: String, e: Throwable? = null) {
        Log.e(tag, msg, e)
    }
}