package com.tgwgroup.zhoupics.ui.edit

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.history.HistoryHelper
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditViewModel : ViewModel() {
    private val bottomTabItemListMutable = MutableStateFlow<List<BottomTabItem>>(emptyList())
    val bottomTabItemList: StateFlow<List<BottomTabItem>> = bottomTabItemListMutable

    private val selectedBottomTabIdMutable = MutableStateFlow(TAB_ADJUST)
    val selectedBottomTabId: StateFlow<Int> = selectedBottomTabIdMutable

    val historyHelper = HistoryHelper()

    init {
        bottomTabItemListMutable.value = listOf(
            BottomTabItem(
                id = TAB_COMPOSITION,
                name = appContext.getString(R.string.composition),
                onClick = {
                    selectBottomTab(TAB_COMPOSITION)
                }
            ),
            BottomTabItem(
                id = TAB_ELIMINATE,
                name = appContext.getString(R.string.eliminate),
                onClick = {
                    selectBottomTab(TAB_ELIMINATE)
                }
            ),
            BottomTabItem(
                id = TAB_BEAUTIFY,
                name = appContext.getString(R.string.beautify),
                onClick = {
                    selectBottomTab(TAB_BEAUTIFY)
                }
            ),
            BottomTabItem(
                id = TAB_ADJUST,
                name = appContext.getString(R.string.adjust),
                selected = true,
                onClick = {
                    selectBottomTab(TAB_ADJUST)
                }
            ),
            BottomTabItem(
                id = TAB_FILTER,
                name = appContext.getString(R.string.filter),
                onClick = {
                    selectBottomTab(TAB_FILTER)
                }
            ),
            BottomTabItem(
                id = TAB_COMPARE_FACES,
                name = appContext.getString(R.string.compare_faces),
                onClick = {
                    selectBottomTab(TAB_COMPARE_FACES)
                }
            )
        )
    }

    private fun selectBottomTab(id: Int) {
        if (id == selectedBottomTabIdMutable.value) {
            return
        }
        bottomTabItemListMutable.value = bottomTabItemListMutable.value.map { item ->
            item.copy(selected = item.id == id)
        }
        selectedBottomTabIdMutable.value = id
    }
}