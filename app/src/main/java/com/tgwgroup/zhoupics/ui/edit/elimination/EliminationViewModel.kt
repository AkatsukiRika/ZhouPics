package com.tgwgroup.zhoupics.ui.edit.elimination

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.tgwgroup.inpaintlib.InpaintLib
import com.tgwgroup.zhoupics.ui.downloads.getEliminateModelItem
import com.tgwgroup.zhoupics.utils.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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

    private val canGenerateMutable = MutableStateFlow(false)
    val canGenerate = canGenerateMutable.asStateFlow()

    private val inpaintResultEventMutable = MutableSharedFlow<InpaintResultEvent>()
    val inpaintResultEvent = inpaintResultEventMutable.asSharedFlow()

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

    suspend fun runInpaint(image: Bitmap, mask: Bitmap) = withContext(Dispatchers.Default) {
        val result = InpaintLib.runInpaint(image, mask, getEliminateModelItem().getOutputFile(appContext).absolutePath)
        if (result != null) {
            inpaintResultEventMutable.emit(InpaintResultEvent.Success(result))
        } else {
            inpaintResultEventMutable.emit(InpaintResultEvent.Error)
        }
    }
}

sealed class InpaintResultEvent {
    data class Success(val bitmap: Bitmap) : InpaintResultEvent()
    data object Error : InpaintResultEvent()
}