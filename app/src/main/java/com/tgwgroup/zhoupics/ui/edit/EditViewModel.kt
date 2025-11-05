package com.tgwgroup.zhoupics.ui.edit

import androidx.lifecycle.ViewModel
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.history.HistoryHelper
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditViewModel : ViewModel() {
    private val bottomTabItemListMutable = MutableStateFlow<List<BottomTabItem>>(emptyList())
    val bottomTabItemList = bottomTabItemListMutable.asStateFlow()

    private val selectedBottomTabIdMutable = MutableStateFlow(TAB_ADJUST)
    val selectedBottomTabId = selectedBottomTabIdMutable.asStateFlow()

    private val inRoomMutable = MutableStateFlow(false)
    val inRoom = inRoomMutable.asStateFlow()

    private var lastBottomTabId: Int = TAB_ADJUST

    val historyHelper = HistoryHelper()

    var onCompareFacesClicked: (() -> Unit)? = null

    var onCompositionClicked: (() -> Unit)? = null

    var onEliminationClicked: (() -> Unit)? = null

    init {
        bottomTabItemListMutable.value = listOf(
            BottomTabItem(
                id = TAB_COMPOSITION,
                name = appContext.getString(R.string.composition),
                onClick = {
                    onCompositionClicked?.invoke()
                }
            ),
            BottomTabItem(
                id = TAB_ELIMINATE,
                name = appContext.getString(R.string.elimination),
                onClick = {
                    onEliminationClicked?.invoke()
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
                    onCompareFacesClicked?.invoke()
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
        lastBottomTabId = selectedBottomTabIdMutable.value
        selectedBottomTabIdMutable.value = id
    }

    fun updateInRoom(value: Boolean) {
        inRoomMutable.value = value
    }
}