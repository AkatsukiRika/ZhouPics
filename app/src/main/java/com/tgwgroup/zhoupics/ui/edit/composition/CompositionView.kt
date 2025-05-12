package com.tgwgroup.zhoupics.ui.edit.composition

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.dpToPx

class CompositionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    enum class DragMode {
        TOP_START, TOP_CENTER, TOP_END,
        CENTER_START, CENTER_END,
        BOTTOM_START, BOTTOM_CENTER, BOTTOM_END
    }

    companion object {
        const val TAG = "CompositionView"
    }

    private var viewWidth = 0
    private var viewHeight = 0

    private var imageBitmap: Bitmap? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageRect = Rect()

    private var originalSecondaryBitmap: Bitmap? = null
    private var originalSecondaryCanvas: Canvas? = null
    private var rotatedSecondaryBitmap: Bitmap? = null
    private var rotatedSecondaryCanvas: Canvas? = null

    private var rotationDegrees = 0
    private val imageTransformMatrix = Matrix()

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

    var cropMode = CROP_FREEFORM
        set(value) {
            field = value
            invalidate()
        }

    private var dragMode: DragMode? = null

    private val handleSize = dpToPx(48f)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        LogUtil.d(TAG, "onSizeChanged: viewWidth=$viewWidth, viewHeight=$viewHeight")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onActionDown(event)
                }
            }
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val left = imageRect.left.toFloat()
        val top = imageRect.top.toFloat()
        val right = imageRect.right.toFloat()
        val bottom = imageRect.bottom.toFloat()
        val centerX = imageRect.exactCenterX()
        val centerY = imageRect.exactCenterY()
        when {
            x >= left && x <= left + handleSize && y >= top && y <= top + handleSize -> {
                dragMode = DragMode.TOP_START
            }

            x >= centerX - handleSize / 2 && x <= centerX + handleSize / 2 && y >= top && y <= top + handleSize -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.TOP_CENTER else null
            }

            x >= right - handleSize && x <= right && y >= top && y <= top + handleSize -> {
                dragMode = DragMode.TOP_END
            }

            x >= left && x <= left + handleSize && y >= centerY - handleSize / 2 && y <= centerY + handleSize / 2 -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.CENTER_START else null
            }

            x >= right - handleSize && x <= right && y >= centerY - handleSize / 2 && y <= centerY + handleSize / 2 -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.CENTER_END else null
            }

            x >= left && x <= left + handleSize && y >= bottom - handleSize && y <= bottom -> {
                dragMode = DragMode.BOTTOM_START
            }

            x >= centerX - handleSize / 2 && x <= centerX + handleSize / 2 && y >= bottom - handleSize && y <= bottom -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.BOTTOM_CENTER else null
            }

            x >= right - handleSize && x <= right && y >= bottom - handleSize && y <= bottom -> {
                dragMode = DragMode.BOTTOM_END
            }

            else -> {
                dragMode = null
            }
        }
        LogUtil.d(TAG, "dragMode = $dragMode")
    }

    private fun createSecondaryCanvas() {
        if (originalSecondaryBitmap == null || originalSecondaryBitmap?.width != imageWidth || originalSecondaryBitmap?.height != imageHeight) {
            originalSecondaryBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            originalSecondaryCanvas = Canvas(originalSecondaryBitmap!!)
        }
        if (rotatedSecondaryBitmap == null || rotatedSecondaryBitmap?.width != imageHeight || rotatedSecondaryBitmap?.height != imageWidth) {
            rotatedSecondaryBitmap = Bitmap.createBitmap(imageHeight, imageWidth, Bitmap.Config.ARGB_8888)
            rotatedSecondaryCanvas = Canvas(rotatedSecondaryBitmap!!)
        }
    }

    private fun rotateImageOnSecondaryCanvas() {
        val imageBitmap = imageBitmap ?: return

        originalSecondaryCanvas?.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        rotatedSecondaryCanvas?.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)

        val centerX = imageBitmap.width / 2f
        val centerY = imageBitmap.height / 2f

        if (rotationDegrees % 180 == 0) {
            val tempMatrix = Matrix()
            tempMatrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)
            originalSecondaryCanvas?.drawBitmap(imageBitmap, tempMatrix, null)
        } else {
            val tempMatrix = Matrix()
            tempMatrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)
            tempMatrix.postTranslate((imageBitmap.height - imageBitmap.width) / 2f, (imageBitmap.width - imageBitmap.height) / 2f)
            rotatedSecondaryCanvas?.drawBitmap(imageBitmap, tempMatrix, null)
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageBitmap = bitmap
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        createSecondaryCanvas()
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
        imageTransformMatrix.reset()
        imageTransformMatrix.postRotate(rotationDegrees.toFloat(), imageRect.centerX().toFloat(), imageRect.centerY().toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawImage(canvas)
        drawBorder(canvas)
        drawCornerHandles(canvas)
        if (cropMode == CROP_FREEFORM) {
            drawMidHandles(canvas)
        }
    }

    private fun drawImage(canvas: Canvas) {
        rotateImageOnSecondaryCanvas()

        val rotatedImageWidth = if (rotationDegrees % 180 == 0) imageWidth else imageHeight
        val rotatedImageHeight = if (rotationDegrees % 180 == 0) imageHeight else imageWidth
        val scale1 = viewWidth.toFloat() / rotatedImageWidth.toFloat()
        val scale2 = viewHeight.toFloat() / rotatedImageHeight.toFloat()

        val scale = minOf(scale1, scale2)
        val scaledWidth = (rotatedImageWidth * scale).toInt()
        val scaledHeight = (rotatedImageHeight * scale).toInt()
        val left = (viewWidth - scaledWidth) / 2
        val top = (viewHeight - scaledHeight) / 2
        imageRect.set(left, top, left + scaledWidth, top + scaledHeight)

        val bitmapToDraw = if (rotationDegrees % 180 == 0) originalSecondaryBitmap else rotatedSecondaryBitmap
        bitmapToDraw?.let {
            canvas.drawBitmap(
                it,
                Rect(0, 0, it.width, it.height),
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
        val strokeWidth = handlePaint.strokeWidth

        val left = imageRect.left.toFloat() + strokeWidth / 2
        val top = imageRect.top.toFloat() + strokeWidth / 2
        val right = imageRect.right.toFloat() - strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - strokeWidth / 2

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
        val strokeWidth = handlePaint.strokeWidth

        val centerX = (imageRect.left.toFloat() + imageRect.right.toFloat()) / 2
        val centerY = (imageRect.top.toFloat() + imageRect.bottom.toFloat()) / 2
        val top = imageRect.top.toFloat() + strokeWidth / 2
        val left = imageRect.left.toFloat() + strokeWidth / 2
        val right = imageRect.right.toFloat() - strokeWidth / 2
        val bottom = imageRect.bottom.toFloat() - strokeWidth / 2

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