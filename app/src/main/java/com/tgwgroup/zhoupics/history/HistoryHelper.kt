package com.tgwgroup.zhoupics.history

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryHelper {
    private val historyRecordList = mutableListOf<HistoryRecord>()
    private var currentIndex = -1

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo

    init {
        addRecord(InitRecord())
    }

    private fun canUndo(): Boolean {
        return currentIndex > 0
    }

    private fun canRedo(): Boolean {
        return currentIndex < historyRecordList.lastIndex
    }

    private fun refreshCanUndoRedo() {
        _canUndo.value = canUndo()
        _canRedo.value = canRedo()
    }

    fun addRecord(record: HistoryRecord) {
        if (currentIndex < historyRecordList.lastIndex) {
            historyRecordList.subList(currentIndex + 1, historyRecordList.size).clear()
        }
        val latestRecord = historyRecordList.lastOrNull()
        if (record != latestRecord) {
            historyRecordList.add(record)
            currentIndex++
        }
        refreshCanUndoRedo()
    }

    fun getLatestRecord(type: Class<out HistoryRecord>): HistoryRecord? {
        val subList = historyRecordList.subList(0, currentIndex + 1)
        return subList.lastOrNull { it::class.java == type }
    }
}