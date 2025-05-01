package com.tgwgroup.zhoupics.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.withMatrix

class MatrixTransformLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var transformMatrix: Matrix = Matrix()

    fun setTransformMatrix(matrix: Matrix) {
        transformMatrix.set(matrix)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withMatrix(transformMatrix) {
            super.dispatchDraw(this)
        }
    }
}