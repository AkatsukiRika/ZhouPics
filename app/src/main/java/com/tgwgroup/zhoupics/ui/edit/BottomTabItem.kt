package com.tgwgroup.zhoupics.ui.edit

data class BottomTabItem(
    val id: Int,
    val name: String,
    var selected: Boolean = false,
    val onClick: () -> Unit
)

const val TAB_COMPOSITION = 0
const val TAB_ELIMINATE = 1
const val TAB_BEAUTIFY = 2
const val TAB_ADJUST = 3
const val TAB_FILTER = 4
const val TAB_COMPARE_FACES = 5