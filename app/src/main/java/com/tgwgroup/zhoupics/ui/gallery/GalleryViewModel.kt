package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.utils.getAlbumList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class GalleryViewModel : ViewModel() {
    private val albumListMutable = MutableStateFlow<List<AlbumItem>>(emptyList())
    val albumList: StateFlow<List<AlbumItem>> = albumListMutable

    private val selectedAlbumIdMutable = MutableStateFlow<String?>(null)

    private val selectedAlbumIndexMutable = MutableStateFlow(0)

    private val loadingMutable = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = loadingMutable

    suspend fun queryAlbums(
        context: Context,
        onClickImage: ((ImageClickEvent, Uri) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        albumListMutable.value = getAlbumList(context, ::selectAlbum, onClickImage)
        loadingMutable.value = false
    }

    /**
     * @param id null means all photos
     */
    private fun selectAlbum(id: String?) {
        if (id == selectedAlbumIdMutable.value) {
            return
        }
        albumListMutable.value = albumListMutable.value.map {
            it.copy(selected = it.id == id)
        }
        selectedAlbumIdMutable.value = id
    }

    fun selectAlbumByIndex(index: Int) {
        if (index == selectedAlbumIndexMutable.value) {
            return
        }
        selectedAlbumIndexMutable.value = index
        if (index in albumListMutable.value.indices) {
            val selectedAlbum = albumListMutable.value[index]
            selectAlbum(selectedAlbum.id)
        }
    }
}