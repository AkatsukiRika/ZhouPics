package com.tgwgroup.zhoupics.ui.edit.composition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.dpToPx

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawImage(canvas)
        drawBorder(canvas)
        drawCornerHandles(canvas)
        if (drawMidHandles) {
            drawMidHandles(canvas)
        }
    }

    private fun drawImage(canvas: Canvas) {
        val viewLongSide = maxOf(viewWidth, viewHeight)
        val imageLongSide = maxOf(imageWidth, imageHeight)
        val scale1 = viewLongSide.toFloat() / imageLongSide.toFloat()

        val viewShortSide = minOf(viewWidth, viewHeight)
        val imageShortSide = minOf(imageWidth, imageHeight)
        val scale2 = viewShortSide.toFloat() / imageShortSide.toFloat()

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
        val handleSize = dpToPx(48f)
        val left = imageRect.left.toFloat() + handlePaint.strokeWidth / 2
        val top = imageRect.top.toFloat() + handlePaint.strokeWidth / 2
        val right = imageRect.right.toFloat() - handlePaint.strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - handlePaint.strokeWidth / 2

        canvas.drawLine(left, top, left + handleSize, top, handlePaint)
        canvas.drawLine(left, top, left, top + handleSize, handlePaint)
        canvas.drawLine(right, top, right - handleSize, top, handlePaint)
        canvas.drawLine(right, top, right, top + handleSize, handlePaint)

        canvas.drawLine(left, bottom, left + handleSize, bottom, handlePaint)
        canvas.drawLine(left, bottom, left, bottom - handleSize, handlePaint)
        canvas.drawLine(right, bottom, right - handleSize, bottom, handlePaint)
        canvas.drawLine(right, bottom, right, bottom - handleSize, handlePaint)
    }

    private fun drawMidHandles(canvas: Canvas) {
        val handleSize = dpToPx(48f)
        val centerX = (imageRect.left.toFloat() + imageRect.right.toFloat()) / 2
        val centerY = (imageRect.top.toFloat() + imageRect.bottom.toFloat()) / 2
        val top = imageRect.top.toFloat() + handlePaint.strokeWidth / 2
        val left = imageRect.left.toFloat() + handlePaint.strokeWidth / 2
        val right = imageRect.right.toFloat() - handlePaint.strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - handlePaint.strokeWidth / 2

        canvas.drawLine(centerX, top, centerX - handleSize / 2, top, handlePaint)
        canvas.drawLine(centerX, top, centerX + handleSize / 2, top, handlePaint)

        canvas.drawLine(left, centerY, left, centerY - handleSize / 2, handlePaint)
        canvas.drawLine(left, centerY, left, centerY + handleSize / 2, handlePaint)

        canvas.drawLine(right, centerY, right, centerY - handleSize / 2, handlePaint)
        canvas.drawLine(right, centerY, right, centerY + handleSize / 2, handlePaint)

        canvas.drawLine(centerX, bottom, centerX - handleSize / 2, bottom, handlePaint)
        canvas.drawLine(centerX, bottom, centerX + handleSize / 2, bottom, handlePaint)
    }
}