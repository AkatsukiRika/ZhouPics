package com.tgwgroup.zhoupics.ui.downloads

import android.content.Context
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.constants.ELIMINATE_MODEL_NAME
import com.tgwgroup.zhoupics.constants.HOSTING_BASE_URL
import com.tgwgroup.zhoupics.constants.getModelDir
import com.tgwgroup.zhoupics.utils.appContext
import com.tgwgroup.zhoupics.utils.calculateMD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class DownloadsItem(
    val index: Int,
    val title: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val fileMD5: String,
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

    fun getUrl() = "$HOSTING_BASE_URL$fileName"

    fun getOutputFile(context: Context) = File(getModelDir(context), fileName)

    suspend fun hasLocalFile(context: Context): Boolean = withContext(Dispatchers.IO) {
        val file = getOutputFile(context)
        val md5 = calculateMD5(file)
        file.exists() && md5 != null && md5.equals(fileMD5, ignoreCase = true)
    }
}

fun getEliminateModelItem(onClick: (DownloadsItem) -> Unit = {}): DownloadsItem {
    return DownloadsItem(
        index = 0,
        title = appContext.getString(R.string.elimination_model),
        fileName = ELIMINATE_MODEL_NAME,
        fileSizeBytes = 28265660L,
        fileMD5 = "62ba6158a0c769af78581d8405815c31",
        onClick = onClick
    )
}

enum class DownloadStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}