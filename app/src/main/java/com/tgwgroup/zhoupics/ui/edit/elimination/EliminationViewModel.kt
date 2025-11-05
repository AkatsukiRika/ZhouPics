package com.tgwgroup.zhoupics.ui.edit.elimination

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgwgroup.inpaintlib.InpaintLib
import com.tgwgroup.zhoupics.ui.downloads.getEliminateModelItem
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EliminationViewModel : ViewModel() {
    enum class Mode {
        PAINT, LARIAT, ERASER
    }

    enum class Status {
        IDLE, LOADING, SUCCESS, ERROR
    }

    private val currentModeMutable = MutableStateFlow(Mode.PAINT)
    val currentMode = currentModeMutable.asStateFlow()

    private val paintSizeMutable = MutableStateFlow(50f)
    val paintSize = paintSizeMutable.asStateFlow()

    private val eraserSizeMutable = MutableStateFlow(50f)
    val eraserSize = eraserSizeMutable.asStateFlow()

    private val canGenerateMutable = MutableStateFlow(false)
    val canGenerate = canGenerateMutable.asStateFlow()

    private val inpaintStatusMutable = MutableStateFlow(Status.IDLE)
    val inpaintStatus = inpaintStatusMutable.asStateFlow()

    fun updateCurrentMode(mode: Mode) {
        currentModeMutable.value = mode
    }

    fun updatePaintSize(size: Float) {
        paintSizeMutable.value = size
    }

    fun updateEraserSize(size: Float) {
        eraserSizeMutable.value = size
    }

    fun updateCanGenerate(canGenerate: Boolean) {
        canGenerateMutable.value = canGenerate
    }

    fun runInpaint(image: Bitmap, mask: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            inpaintStatusMutable.emit(Status.LOADING)
            val result = InpaintLib.runInpaint(image, mask, getEliminateModelItem().getOutputFile(appContext).absolutePath)
            if (result != null) {
                inpaintStatusMutable.emit(Status.SUCCESS)
            } else {
                inpaintStatusMutable.emit(Status.ERROR)
            }
        }
    }
}