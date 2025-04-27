package com.tgwgroup.zhoupics

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.tgwgroup.zhoupics.utils.appContext

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this.applicationContext
        // 强制设置深色主题，忽略系统设置
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
} 