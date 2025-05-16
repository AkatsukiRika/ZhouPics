package com.tgwgroup.zhoupics.ui.edit.filter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class FilterItem(
    val id: Int,
    @DrawableRes val icon: Int,
    @ColorInt val labelBgColor: Int,
    val name: String,
    var selected: Boolean = false,
    var progress: Float = 100f,
    val onClick: () -> Unit
)

const val FILTER_ORIGINAL = 0
const val FILTER_FAIRY_TALE = 1
const val FILTER_SUNRISE = 2
const val FILTER_SUNSET = 3
const val FILTER_WHITE_CAT = 4
const val FILTER_BLACK_CAT = 5
const val FILTER_BEAUTY = 6
const val FILTER_SKIN_WHITEN = 7
const val FILTER_HEALTHY = 8