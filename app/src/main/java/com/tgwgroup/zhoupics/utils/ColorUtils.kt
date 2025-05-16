package com.tgwgroup.zhoupics.utils

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

fun @receiver:ColorInt Int.withAlpha(percent: Int): Int {
    val alpha = (percent.coerceIn(0, 100) * 2.55f).roundToInt()
    return ColorUtils.setAlphaComponent(this, alpha)
}