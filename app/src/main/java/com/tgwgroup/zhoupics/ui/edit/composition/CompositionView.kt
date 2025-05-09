package com.tgwgroup.zhoupics.ui.edit.composition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withMatrix
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.dpToPx
import com.tgwgroup.zhoupics.utils.getParams

class CompositionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val TAG = "CompositionView"
    }

    private var viewWidth = 0
    private var viewHeight = 0

    private var imageBitmap: Bitmap? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageRect = Rect()

    private var rotationDegrees = 0
    private val frameTransformMatrix = Matrix()

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = dpToPx(2f).toFloat()
        color = resources.getColor(R.color.white, null)
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = dpToPx(4f).toFloat()
        color = resources.getColor(R.color.white, null)
    }

    var drawMidHandles = true
        set(value) {
            field = value
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        LogUtil.d(TAG, "onSizeChanged: viewWidth=$viewWidth, viewHeight=$viewHeight")
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageBitmap = bitmap
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        LogUtil.d(TAG, "setImageBitmap: imageWidth=$imageWidth, imageHeight=$imageHeight")
        invalidate()
    }

    fun rotateLeft() {
        rotationDegrees -= 90
        updateFrameTransformMatrix()
    }

    fun rotateRight() {
        rotationDegrees += 90
        updateFrameTransformMatrix()
    }

    private fun updateFrameTransformMatrix() {
        frameTransformMatrix.reset()
        frameTransformMatrix.postRotate(rotationDegrees.toFloat(), imageRect.centerX().toFloat(), imageRect.centerY().toFloat())
        if (rotationDegrees % 180 != 0) {
            val imageRectWidth = imageRect.width()
            val imageRectHeight = imageRect.height()
            val scale1 = viewWidth.toFloat() / imageRectHeight.toFloat()
            val scale2 = viewHeight.toFloat() / imageRectWidth.toFloat()
            val scale = minOf(scale1, scale2)
            frameTransformMatrix.postScale(scale, scale, imageRect.centerX().toFloat(), imageRect.centerY().toFloat())
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withMatrix(frameTransformMatrix) {
            super.onDraw(canvas)
            drawImage(canvas)
            drawBorder(canvas)
            drawCornerHandles(canvas)
            if (drawMidHandles) {
                drawMidHandles(canvas)
            }
        }
    }

    private fun drawImage(canvas: Canvas) {
        val scale1 = viewWidth.toFloat() / imageWidth.toFloat()
        val scale2 = viewHeight.toFloat() / imageHeight.toFloat()

        val scale = minOf(scale1, scale2)
        val scaledWidth = (imageWidth * scale).toInt()
        val scaledHeight = (imageHeight * scale).toInt()
        val left = (viewWidth - scaledWidth) / 2
        val top = (viewHeight - scaledHeight) / 2
        imageRect.set(left, top, left + scaledWidth, top + scaledHeight)
        imageBitmap?.let {
            canvas.drawBitmap(
                it,
                Rect(0, 0, imageWidth, imageHeight),
                imageRect,
                null
            )
        }
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawRect(
            imageRect.left.toFloat() + borderPaint.strokeWidth / 2,
            imageRect.top.toFloat() + borderPaint.strokeWidth / 2,
            imageRect.right.toFloat() - borderPaint.strokeWidth / 2,
            imageRect.bottom.toFloat() - borderPaint.strokeWidth / 2,
            borderPaint
        )
    }

    private fun drawCornerHandles(canvas: Canvas) {
        val frameScaleX = frameTransformMatrix.getParams().scaleX
        val handleSize = dpToPx(48f) / frameScaleX
        val strokeWidth = handlePaint.strokeWidth / frameScaleX

        val left = imageRect.left.toFloat() + strokeWidth / 2
        val top = imageRect.top.toFloat() + strokeWidth / 2
        val right = imageRect.right.toFloat() - strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - strokeWidth / 2

        val tempHandlePaint = Paint(handlePaint).apply {
            this.strokeWidth = strokeWidth
        }

        canvas.drawLine(left, top, left + handleSize, top, tempHandlePaint)
        canvas.drawLine(left, top, left, top + handleSize, tempHandlePaint)
        canvas.drawLine(right, top, right - handleSize, top, tempHandlePaint)
        canvas.drawLine(right, top, right, top + handleSize, tempHandlePaint)

        canvas.drawLine(left, bottom, left + handleSize, bottom, tempHandlePaint)
        canvas.drawLine(left, bottom, left, bottom - handleSize, tempHandlePaint)
        canvas.drawLine(right, bottom, right - handleSize, bottom, tempHandlePaint)
        canvas.drawLine(right, bottom, right, bottom - handleSize, tempHandlePaint)
    }

    private fun drawMidHandles(canvas: Canvas) {
        val frameScaleX = frameTransformMatrix.getParams().scaleX
        val handleSize = dpToPx(48f) / frameScaleX
        val strokeWidth = handlePaint.strokeWidth / frameScaleX

        val centerX = (imageRect.left.toFloat() + imageRect.right.toFloat()) / 2
        val centerY = (imageRect.top.toFloat() + imageRect.bottom.toFloat()) / 2
        val top = imageRect.top.toFloat() + strokeWidth / 2
        val left = imageRect.left.toFloat() + strokeWidth / 2
        val right = imageRect.right.toFloat() - strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - strokeWidth / 2

        val tempHandlePaint = Paint(handlePaint).apply {
            this.strokeWidth = strokeWidth
        }

        canvas.drawLine(centerX, top, centerX - handleSize / 2, top, tempHandlePaint)
        canvas.drawLine(centerX, top, centerX + handleSize / 2, top, tempHandlePaint)

        canvas.drawLine(left, centerY, left, centerY - handleSize / 2, tempHandlePaint)
        canvas.drawLine(left, centerY, left, centerY + handleSize / 2, tempHandlePaint)

        canvas.drawLine(right, centerY, right, centerY - handleSize / 2, tempHandlePaint)
        canvas.drawLine(right, centerY, right, centerY + handleSize / 2, tempHandlePaint)

        canvas.drawLine(centerX, bottom, centerX - handleSize / 2, bottom, tempHandlePaint)
        canvas.drawLine(centerX, bottom, centerX + handleSize / 2, bottom, tempHandlePaint)
    }
}