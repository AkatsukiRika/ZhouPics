package com.tgwgroup.zhoupics.ui.settings.language

import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext

data class LanguageItem(
    val name: String,
    val displayName: String
) {
    var selected = false
}

fun getSupportLanguageList() = listOf(
    LanguageItem("system", appContext.resources.getString(R.string.system_default)).apply {
        selected = true
    },
    LanguageItem("en", "English"),
    LanguageItem("zh-CN", "简体中文"),
    LanguageItem("zh-TW", "繁體中文"),
    LanguageItem("ja", "日本語"),
    LanguageItem("ko", "한국어")
)