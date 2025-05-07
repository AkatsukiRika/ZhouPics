package com.tgwgroup.zhoupics.ui.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.utils.galleryLoading
import com.tgwgroup.zhoupics.utils.preloadedAlbumList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    suspend fun queryAlbums(onClickImage: ((ImageClickEvent, Uri) -> Unit)? = null) = withContext(Dispatchers.IO) {
        if (!galleryLoading.value) {
            delay(500)
            buildAlbumList(onClickImage)
        } else {
            galleryLoading.collect {
                if (!it) {
                    buildAlbumList(onClickImage)
                }
            }
        }
    }

    private fun buildAlbumList(onClickImage: ((ImageClickEvent, Uri) -> Unit)?) {
        val albumList = mutableListOf<AlbumItem>()
        preloadedAlbumList.forEach { albumItem ->
            val imageList = mutableListOf<ImageItem>()
            albumItem.images.forEach { imageItem ->
                imageList.add(imageItem.copy())
            }
            albumList.add(albumItem.copy(images = imageList))
        }
        albumList.forEachIndexed { index, album ->
            album.onClick = {
                selectAlbum(album.id)
            }
            album.selected = index == 0

            album.images.forEach { image ->
                image.onClick = { event ->
                    onClickImage?.invoke(event, image.uri)
                }
            }
        }
        albumListMutable.value = albumList
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