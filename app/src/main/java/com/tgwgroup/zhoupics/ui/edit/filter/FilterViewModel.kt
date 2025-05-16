package com.tgwgroup.zhoupics.ui.edit.filter

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilterViewModel : ViewModel() {
    private val itemListMutable = MutableStateFlow<List<FilterItem>>(emptyList())
    val itemList: StateFlow<List<FilterItem>> = itemListMutable

    private val selectedItemIdMutable = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = selectedItemIdMutable

    fun init() {
        itemListMutable.value = listOf(
            FilterItem(
                id = FILTER_ORIGINAL,
                icon = R.drawable.img_filter_original,
                labelBgColor = appContext.getColor(R.color.filter_color_grey_light),
                name = appContext.getString(R.string.filter_original),
                onClick = {
                    selectItem(FILTER_ORIGINAL)
                }
            ),
            FilterItem(
                id = FILTER_FAIRY_TALE,
                icon = R.drawable.img_filter_fairy_tale,
                labelBgColor = appContext.getColor(R.color.filter_color_blue),
                name = appContext.getString(R.string.filter_fairy_tale),
                onClick = {
                    selectItem(FILTER_FAIRY_TALE)
                }
            ),
            FilterItem(
                id = FILTER_SUNRISE,
                icon = R.drawable.img_filter_sunrise,
                labelBgColor = appContext.getColor(R.color.filter_color_brown_light),
                name = appContext.getString(R.string.filter_sunrise),
                onClick = {
                    selectItem(FILTER_SUNRISE)
                }
            ),
            FilterItem(
                id = FILTER_SUNSET,
                icon = R.drawable.img_filter_sunset,
                labelBgColor = appContext.getColor(R.color.filter_color_brown_light),
                name = appContext.getString(R.string.filter_sunset),
                onClick = {
                    selectItem(FILTER_SUNSET)
                }
            ),
            FilterItem(
                id = FILTER_WHITE_CAT,
                icon = R.drawable.img_filter_white_cat,
                labelBgColor = appContext.getColor(R.color.filter_color_brown_light),
                name = appContext.getString(R.string.filter_white_cat),
                onClick = {
                    selectItem(FILTER_WHITE_CAT)
                }
            ),
            FilterItem(
                id = FILTER_BLACK_CAT,
                icon = R.drawable.img_filter_black_cat,
                labelBgColor = appContext.getColor(R.color.filter_color_brown_light),
                name = appContext.getString(R.string.filter_black_cat),
                onClick = {
                    selectItem(FILTER_BLACK_CAT)
                }
            ),
            FilterItem(
                id = FILTER_BEAUTY,
                icon = R.drawable.img_filter_beauty,
                labelBgColor = appContext.getColor(R.color.filter_color_red),
                name = appContext.getString(R.string.filter_beauty),
                onClick = {
                    selectItem(FILTER_BEAUTY)
                }
            ),
            FilterItem(
                id = FILTER_SKIN_WHITEN,
                icon = R.drawable.img_filter_beauty,
                labelBgColor = appContext.getColor(R.color.filter_color_red),
                name = appContext.getString(R.string.filter_whitening),
                onClick = {
                    selectItem(FILTER_SKIN_WHITEN)
                }
            ),
            FilterItem(
                id = FILTER_HEALTHY,
                icon = R.drawable.img_filter_healthy,
                labelBgColor = appContext.getColor(R.color.filter_color_red),
                name = appContext.getString(R.string.filter_healthy),
                onClick = {
                    selectItem(FILTER_HEALTHY)
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