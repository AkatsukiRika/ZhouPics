package com.tgwgroup.zhoupics.history

import com.google.gson.Gson
import com.tgwgroup.zhoupics.utils.LogUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryHelper {
    companion object {
        const val TAG = "HistoryHelper"
    }

    private val historyRecordList = mutableListOf<HistoryRecord>()
    private var currentIndex = -1
    private val scope = MainScope()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo

    private val _undoEvent = MutableSharedFlow<UndoEvent>(replay = 0)
    val undoEvent: SharedFlow<UndoEvent> = _undoEvent

    private val _redoEvent = MutableSharedFlow<RedoEvent>(replay = 0)
    val redoEvent: SharedFlow<RedoEvent> = _redoEvent

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
            printLog()
        }
        refreshCanUndoRedo()
    }

    fun undo() {
        if (!canUndo()) {
            return
        }
        currentIndex--
        scope.launch {
            if (currentIndex in historyRecordList.indices) {
                _undoEvent.emit(UndoEvent(historyRecordList[currentIndex]))
            }
        }
        refreshCanUndoRedo()
    }

    fun redo() {
        if (!canRedo()) {
            return
        }
        currentIndex++
        scope.launch {
            if (currentIndex in historyRecordList.indices) {
                _redoEvent.emit(RedoEvent(historyRecordList[currentIndex]))
            }
        }
        refreshCanUndoRedo()
    }

    fun getLatestRecord(type: Class<out HistoryRecord>): HistoryRecord? {
        val subList = historyRecordList.subList(0, currentIndex + 1)
        return subList.lastOrNull { it::class.java == type }
    }

    fun isBeforeEarliestRecord(type: Class<out HistoryRecord>): Boolean {
        val earliestIndex = historyRecordList.indexOfFirst { it::class.java == type }
        return earliestIndex != -1 && earliestIndex > currentIndex
    }

    private fun printLog() {
        LogUtil.d(TAG, "currentIndex = $currentIndex")
        LogUtil.d(TAG, Gson().toJson(historyRecordList))
    }
}

data class UndoEvent(val receivedRecord: HistoryRecord)

data class RedoEvent(val receivedRecord: HistoryRecord)