package com.tgwgroup.zhoupics.ui.edit.adjust

import androidx.annotation.DrawableRes

data class AdjustItem(
    val id: Int,
    @DrawableRes val icon: Int,
    val name: String,
    val twoWaySlider: Boolean,
    var selected: Boolean = false,
    val onClick: () -> Unit
)

const val ADJUST_CONTRAST = 0
const val ADJUST_EXPOSURE = 1
const val ADJUST_SATURATION = 2
const val ADJUST_SHARPNESS = 3
const val ADJUST_BRIGHTNESS = 4