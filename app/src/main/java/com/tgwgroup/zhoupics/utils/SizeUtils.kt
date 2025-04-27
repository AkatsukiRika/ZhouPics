package com.tgwgroup.zhoupics.utils

fun dpToPx(dp: Float): Int {
    return (dp * appContext.resources.displayMetrics.density).toInt()
}