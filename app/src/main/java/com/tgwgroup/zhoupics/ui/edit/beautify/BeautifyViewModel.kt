package com.tgwgroup.zhoupics.ui.edit.beautify

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.history.BeautifyRecord
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BeautifyViewModel : ViewModel() {
    private val itemListMutable = MutableStateFlow<List<BeautifyItem>>(emptyList())
    val itemList: StateFlow<List<BeautifyItem>> = itemListMutable

    private val selectedItemIdMutable = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = selectedItemIdMutable

    fun init(latestRecord: BeautifyRecord?) {
        itemListMutable.value = listOf(
            BeautifyItem(
                id = BEAUTIFY_SMOOTH,
                icon = R.drawable.ic_smooth,
                name = appContext.getString(R.string.smooth),
                progress = latestRecord?.smoothProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_SMOOTH)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_WHITE,
                icon = R.drawable.ic_white,
                name = appContext.getString(R.string.white),
                progress = latestRecord?.whiteProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_WHITE)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_LIPSTICK,
                icon = R.drawable.ic_lipstick,
                name = appContext.getString(R.string.lipstick),
                progress = latestRecord?.lipstickProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_LIPSTICK)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_BLUSHER,
                icon = R.drawable.ic_blusher,
                name = appContext.getString(R.string.blusher),
                progress = latestRecord?.blusherProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_BLUSHER)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_EYE_ZOOM,
                icon = R.drawable.ic_eye_zoom,
                name = appContext.getString(R.string.eye_zoom),
                progress = latestRecord?.eyeZoomProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_EYE_ZOOM)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_FACE_SLIM,
                icon = R.drawable.ic_face_slim,
                name = appContext.getString(R.string.face_slim),
                progress = latestRecord?.faceSlimProgress ?: 0f,
                onClick = {
                    selectItem(BEAUTIFY_FACE_SLIM)
                }
            )
        )
    }

    private fun selectItem(id: Int) {
        if (id == selectedItemIdMutable.value) {
            return
        } else {
            itemListMutable.value = itemListMutable.value.map {
                it.copy(selected = it.id == id)
            }
            selectedItemIdMutable.value = id
        }
    }

    fun updateProgress(itemId: Int, progress: Float) {
        itemListMutable.value = itemListMutable.value.map {
            if (it.id == itemId && it.progress != progress) {
                it.copy(progress = progress)
            } else {
                it
            }
        }
    }

    fun getHistoryRecord(): BeautifyRecord {
        val itemList = itemList.value
        return BeautifyRecord(
            smoothProgress = itemList.find { it.id == BEAUTIFY_SMOOTH }?.progress ?: 0f,
            whiteProgress = itemList.find { it.id == BEAUTIFY_WHITE }?.progress ?: 0f,
            lipstickProgress = itemList.find { it.id == BEAUTIFY_LIPSTICK }?.progress ?: 0f,
            blusherProgress = itemList.find { it.id == BEAUTIFY_BLUSHER }?.progress ?: 0f,
            eyeZoomProgress = itemList.find { it.id == BEAUTIFY_EYE_ZOOM }?.progress ?: 0f,
            faceSlimProgress = itemList.find { it.id == BEAUTIFY_FACE_SLIM }?.progress ?: 0f
        )
    }
}