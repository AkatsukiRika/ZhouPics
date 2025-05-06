package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
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
    val selectedAlbumId: StateFlow<String?> = selectedAlbumIdMutable

    private val loadingMutable = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = loadingMutable

    suspend fun queryAlbums(context: Context) = withContext(Dispatchers.IO) {
        albumListMutable.value = getAlbumList(context, ::selectItem)
        loadingMutable.value = false
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