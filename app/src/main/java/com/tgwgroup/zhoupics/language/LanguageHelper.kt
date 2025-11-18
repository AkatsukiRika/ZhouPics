package com.tgwgroup.zhoupics.language

import android.content.Context
import android.content.res.Configuration
import com.tgwgroup.zhoupics.utils.appContext
import androidx.core.content.edit
import com.tgwgroup.zhoupics.ui.settings.language.LANG_SYSTEM
import java.util.Locale

/**
 * 保存语言并生成带Locale的Context
 */
object LanguageHelper {
    private const val PREF_NAME = "app_language"
    private const val KEY_LANGUAGE = "selected_language"

    fun saveLanguage(language: String) {
        appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit(commit = true) {
            putString(KEY_LANGUAGE, language)
        }
    }

    fun getSavedLanguage(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
    }

    fun applyLanguage(context: Context): Context {
        val lang = getSavedLanguage(context) ?: return context
        if (lang == LANG_SYSTEM) {
            return context
        }
        val locale = Locale.forLanguageTag(lang)

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}