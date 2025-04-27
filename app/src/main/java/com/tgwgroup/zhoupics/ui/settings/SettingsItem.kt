package com.tgwgroup.zhoupics.ui.settings

import androidx.annotation.DrawableRes

/**
 * 设置项数据类
 *
 * @param icon 设置项图标资源ID
 * @param title 设置项标题
 * @param showNext 是否显示下一步图标
 * @param onClick 点击设置项的回调
 */
data class SettingsItem(
    @DrawableRes val icon: Int,
    val title: String,
    val showNext: Boolean = true,
    val onClick: () -> Unit
) 