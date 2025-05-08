package com.tgwgroup.zhoupics.ui.edit.composition

import androidx.annotation.DrawableRes

data class CropItem(
    val id: Int,
    @DrawableRes val icon: Int,
    val name: String,
    var selected: Boolean = false,
    val onClick: () -> Unit
)

const val CROP_FREEFORM = 0
const val CROP_ORIGINAL = 1
const val CROP_1_1 = 2
const val CROP_2_3 = 3
const val CROP_3_2 = 4
const val CROP_3_4 = 5
const val CROP_4_3 = 6
const val CROP_9_16 = 7
const val CROP_16_9 = 8