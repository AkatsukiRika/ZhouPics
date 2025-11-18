package com.tgwgroup.zhoupics.language

import android.content.Intent
import android.os.Process.killProcess
import android.os.Process.myPid
import com.tgwgroup.zhoupics.utils.appContext

fun changeLanguage(language: String) {
    LanguageHelper.saveLanguage(language)
    restartApp()
}

fun restartApp() {
    val intent = appContext.packageManager
        .getLaunchIntentForPackage(appContext.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    if (intent != null) {
        appContext.startActivity(intent)
    }
    // 杀掉当前进程，让语言彻底刷新
    killProcess(myPid())
}