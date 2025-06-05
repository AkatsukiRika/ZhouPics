package com.tgwgroup.zhoupics.ui.downloads

import java.util.Locale

data class DownloadsItem(
    val title: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val downloadProgress: Int = 0,
    val onClick: () -> Unit
) {
    fun getHumanizedSize(): String {
        val kb = fileSizeBytes / 1024
        return if (kb < 1024) {
            "$kb KB"
        } else {
            val mb = kb / 1024f
            "${String.format(Locale.ROOT, "%.2f", mb)} MB"
        }
    }
}