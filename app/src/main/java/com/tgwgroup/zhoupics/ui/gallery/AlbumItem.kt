package com.tgwgroup.zhoupics.ui.gallery

import android.net.Uri
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext

data class AlbumItem(
    val id: String?,
    val name: String,
    var selected: Boolean = false,
    var images: List<ImageItem> = emptyList(),
    val onClick: () -> Unit
)

data class ImageItem(
    val name: String,
    val uri: Uri,
    val dateAdded: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val onClick: (event: ImageClickEvent) -> Unit
) {
    fun getHumanizedSize(): String {
        val kb = size / 1024
        return if (kb < 1024) {
            "$kb KB"
        } else {
            val mb = kb / 1024
            "$mb MB"
        }
    }
}

enum class ImageFormat(val displayName: String) {
    JPEG("JPEG"),
    PNG("PNG"),
    GIF("GIF"),
    WEBP("WEBP"),
    HEIC("HEIC"),
    BMP("BMP"),
    UNKNOWN(appContext.getString(R.string.unknown))
}

enum class ImageClickEvent {
    GO_EDIT, ZOOM
}