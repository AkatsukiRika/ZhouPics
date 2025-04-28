package com.tgwgroup.zhoupics.ui.edit.beautify

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BeautifyViewModel : ViewModel() {
    private val itemListMutable = MutableStateFlow<List<BeautifyItem>>(emptyList())
    val itemList: StateFlow<List<BeautifyItem>> = itemListMutable

    private val selectedItemIdMutable = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = selectedItemIdMutable

    init {
        itemListMutable.value = listOf(
            BeautifyItem(
                id = BEAUTIFY_SMOOTH,
                icon = R.drawable.ic_smooth,
                name = appContext.getString(R.string.smooth),
                onClick = {
                    selectItem(BEAUTIFY_SMOOTH)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_WHITE,
                icon = R.drawable.ic_white,
                name = appContext.getString(R.string.white),
                onClick = {
                    selectItem(BEAUTIFY_WHITE)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_LIPSTICK,
                icon = R.drawable.ic_lipstick,
                name = appContext.getString(R.string.lipstick),
                onClick = {
                    selectItem(BEAUTIFY_LIPSTICK)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_BLUSHER,
                icon = R.drawable.ic_blusher,
                name = appContext.getString(R.string.blusher),
                onClick = {
                    selectItem(BEAUTIFY_BLUSHER)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_EYE_ZOOM,
                icon = R.drawable.ic_eye_zoom,
                name = appContext.getString(R.string.eye_zoom),
                onClick = {
                    selectItem(BEAUTIFY_EYE_ZOOM)
                }
            ),
            BeautifyItem(
                id = BEAUTIFY_FACE_SLIM,
                icon = R.drawable.ic_face_slim,
                name = appContext.getString(R.string.face_slim),
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
}