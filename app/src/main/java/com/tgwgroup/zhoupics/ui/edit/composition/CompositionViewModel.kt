package com.tgwgroup.zhoupics.ui.edit.composition

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompositionViewModel : ViewModel() {
    private val itemListMutable = MutableStateFlow<List<CropItem>>(emptyList())
    val itemList: StateFlow<List<CropItem>> = itemListMutable

    init {
        itemListMutable.value = listOf(
            CropItem(
                id = CROP_FREEFORM,
                icon = R.drawable.ic_freeform,
                name = appContext.getString(R.string.freeform),
                onClick = {}
            ),
            CropItem(
                id = CROP_ORIGINAL,
                icon = R.drawable.ic_original,
                name = appContext.getString(R.string.original),
                onClick = {}
            ),
            CropItem(
                id = CROP_1_1,
                icon = R.drawable.ic_ratio_1_1,
                name = "1:1",
                onClick = {}
            ),
            CropItem(
                id = CROP_2_3,
                icon = R.drawable.ic_ratio_2_3,
                name = "2:3",
                onClick = {}
            ),
            CropItem(
                id = CROP_3_2,
                icon = R.drawable.ic_ratio_3_2,
                name = "3:2",
                onClick = {}
            ),
            CropItem(
                id = CROP_3_4,
                icon = R.drawable.ic_ratio_3_4,
                name = "3:4",
                onClick = {}
            ),
            CropItem(
                id = CROP_4_3,
                icon = R.drawable.ic_ratio_4_3,
                name = "4:3",
                onClick = {}
            ),
            CropItem(
                id = CROP_9_16,
                icon = R.drawable.ic_ratio_9_16,
                name = "9:16",
                onClick = {}
            ),
            CropItem(
                id = CROP_16_9,
                icon = R.drawable.ic_ratio_16_9,
                name = "16:9",
                onClick = {}
            )
        )
    }
}