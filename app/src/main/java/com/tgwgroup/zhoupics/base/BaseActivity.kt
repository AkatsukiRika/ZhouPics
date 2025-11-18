package com.tgwgroup.zhoupics.base

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewbinding.ViewBinding
import com.tgwgroup.zhoupics.language.LanguageHelper
import com.tgwgroup.zhoupics.utils.applyStatusBarPaddings

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    override fun attachBaseContext(newBase: Context?) {
        val ctx = newBase?.let {
            LanguageHelper.applyLanguage(it)
        } ?: newBase
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 强制设置深色主题，忽略系统设置
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        // 禁止用户切换主题
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        if (isEdgeToEdgeEnabled()) {
            enableEdgeToEdge()
        }
        binding = onBindingCreate()
        binding.root.applyStatusBarPaddings()
        setContentView(binding.root)
        initView()
    }

    abstract fun onBindingCreate(): T

    open fun initView() {}

    open fun isEdgeToEdgeEnabled() = true
}