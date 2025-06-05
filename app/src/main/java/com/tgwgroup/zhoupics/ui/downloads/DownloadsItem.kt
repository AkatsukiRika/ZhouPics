package com.tgwgroup.zhoupics.ui.downloads

import java.util.Locale

data class DownloadsItem(
    val index: Int,
    val title: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val onClick: (DownloadsItem) -> Unit
) {
    var downloadStatus = DownloadStatus.NOT_STARTED
    var totalBytesRead = 0L

    fun getHumanizedSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb < 1024) {
            "$kb KB"
        } else {
            val mb = kb / 1024f
            "${String.format(Locale.ROOT, "%.2f", mb)} MB"
        }
    }
}

enum class DownloadStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}