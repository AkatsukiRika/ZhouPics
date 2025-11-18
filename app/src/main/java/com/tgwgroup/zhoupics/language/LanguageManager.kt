package com.tgwgroup.zhoupics.language

import android.content.Context
import com.tgwgroup.zhoupics.base.ActivityCollector
import com.tgwgroup.zhoupics.utils.appContext

fun changeLanguage(context: Context, language: String) {
    LanguageHelper.saveLanguage(language)

    runCatching {
        LanguageHelper.applyLanguage(appContext)
    }.onFailure {
        it.printStackTrace()
    }

    ActivityCollector.recreateAll()
}