package com.tgwgroup.zhoupics.ui.gallery

data class AlbumItem(
    val id: String?,
    val name: String,
    var selected: Boolean = false,
    val onClick: () -> Unit
)
