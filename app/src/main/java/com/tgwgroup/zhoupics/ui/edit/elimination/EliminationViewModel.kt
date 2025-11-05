package com.tgwgroup.zhoupics.ui.edit.elimination

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EliminationViewModel : ViewModel() {
    enum class Mode {
        PAINT, LARIAT, ERASER
    }

    private val currentModeMutable = MutableStateFlow(Mode.PAINT)
    val currentMode = currentModeMutable.asStateFlow()

    private val paintSizeMutable = MutableStateFlow(50f)
    val paintSize = paintSizeMutable.asStateFlow()

    private val eraserSizeMutable = MutableStateFlow(50f)
    val eraserSize = eraserSizeMutable.asStateFlow()

    fun updateCurrentMode(mode: Mode) {
        currentModeMutable.value = mode
    }

    fun updatePaintSize(size: Float) {
        paintSizeMutable.value = size
    }

    fun updateEraserSize(size: Float) {
        eraserSizeMutable.value = size
    }
}