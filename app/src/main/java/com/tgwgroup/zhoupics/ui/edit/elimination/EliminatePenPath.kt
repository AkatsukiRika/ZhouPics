package com.tgwgroup.zhoupics.ui.edit.elimination

import android.graphics.Paint
import android.graphics.Path

data class EliminatePenPath(
    val paint: Paint,
    val path: Path,
    val isRestore: Boolean = false,
    val isErase: Boolean = false,
    val isLasso: Boolean = false
)