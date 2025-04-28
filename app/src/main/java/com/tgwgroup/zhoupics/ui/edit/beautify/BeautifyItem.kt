package com.tgwgroup.zhoupics.ui.edit.beautify

import androidx.annotation.DrawableRes

data class BeautifyItem(
    val id: Int,
    @DrawableRes val icon: Int,
    val name: String,
    var selected: Boolean = false,
    val onClick: () -> Unit
)

const val BEAUTIFY_SMOOTH = 0
const val BEAUTIFY_WHITE = 1
const val BEAUTIFY_LIPSTICK = 2
const val BEAUTIFY_BLUSHER = 3
const val BEAUTIFY_EYE_ZOOM = 4
const val BEAUTIFY_FACE_SLIM = 5