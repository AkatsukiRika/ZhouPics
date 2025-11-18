package com.tgwgroup.zhoupics

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.tgwgroup.facecomparelib.AwsUtils
import com.tgwgroup.zhoupics.language.LanguageHelper
import com.tgwgroup.zhoupics.utils.appContext
import com.tgwgroup.zhoupics.utils.initMaxSizeByDeviceLevel
import com.tgwgroup.zhoupics.utils.preloadAlbumList

class App : Application() {
    override fun attachBaseContext(base: Context?) {
        val ctx = base?.let {
            LanguageHelper.applyLanguage(it)
        } ?: base
        super.attachBaseContext(ctx)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        preloadAlbumList()
        initMaxSizeByDeviceLevel()
        AwsUtils.initAwsSdk(appContext)
    }
} 