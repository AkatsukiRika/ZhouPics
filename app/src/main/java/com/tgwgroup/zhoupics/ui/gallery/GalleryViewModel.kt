package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class GalleryViewModel : ViewModel() {
    private val albumListMutable = MutableStateFlow<List<AlbumItem>>(emptyList())
    val albumList: StateFlow<List<AlbumItem>> = albumListMutable

    private val selectedAlbumIdMutable = MutableStateFlow<String?>(null)
    val selectedAlbumId: StateFlow<String?> = selectedAlbumIdMutable

    suspend fun queryAlbums(context: Context) = withContext(Dispatchers.IO) {
        val tempAlbumList = mutableListOf<AlbumItem>()

        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn)

                if (bucketName != null && tempAlbumList.find { it.name == bucketName } == null) {
                    tempAlbumList.add(AlbumItem(
                        id = bucketId,
                        name = bucketName,
                        onClick = {
                            selectItem(bucketId)
                        }
                    ))
                }
            }
        }

        tempAlbumList.add(0, AlbumItem(
            id = null,
            name = context.getString(R.string.all_photos),
            onClick = {
                selectItem(null)
            }
        ))

        if (tempAlbumList.isNotEmpty()) {
            tempAlbumList[0].selected = true
        }

        albumListMutable.value = tempAlbumList
    }

    /**
     * @param id null means all photos
     */
    private fun selectItem(id: String?) {
        if (id == selectedAlbumId.value) {
            return
        }
        albumListMutable.value = albumListMutable.value.map {
            it.copy(selected = it.id == id)
        }
        selectedAlbumIdMutable.value = id
    }
}