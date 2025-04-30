package com.tgwgroup.zhoupics.ui.edit.adjust

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdjustViewModel : ViewModel() {
    private val itemListMutable = MutableStateFlow<List<AdjustItem>>(emptyList())
    val itemList: StateFlow<List<AdjustItem>> = itemListMutable

    private val selectedItemIdMutable = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = selectedItemIdMutable

    init {
        itemListMutable.value = listOf(
            AdjustItem(
                id = ADJUST_CONTRAST,
                icon = R.drawable.ic_contrast,
                name = appContext.getString(R.string.contrast),
                twoWaySlider = true,
                onClick = {
                    selectItem(ADJUST_CONTRAST)
                }
            ),
            AdjustItem(
                id = ADJUST_EXPOSURE,
                icon = R.drawable.ic_exposure,
                name = appContext.getString(R.string.exposure),
                twoWaySlider = true,
                onClick = {
                    selectItem(ADJUST_EXPOSURE)
                }
            ),
            AdjustItem(
                id = ADJUST_SATURATION,
                icon = R.drawable.ic_saturation,
                name = appContext.getString(R.string.saturation),
                twoWaySlider = true,
                onClick = {
                    selectItem(ADJUST_SATURATION)
                }
            ),
            AdjustItem(
                id = ADJUST_SHARPNESS,
                icon = R.drawable.ic_sharpness,
                name = appContext.getString(R.string.sharpness),
                twoWaySlider = false,
                onClick = {
                    selectItem(ADJUST_SHARPNESS)
                }
            ),
            AdjustItem(
                id = ADJUST_BRIGHTNESS,
                icon = R.drawable.ic_brightness,
                name = appContext.getString(R.string.brightness),
                twoWaySlider = true,
                onClick = {
                    selectItem(ADJUST_BRIGHTNESS)
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
}