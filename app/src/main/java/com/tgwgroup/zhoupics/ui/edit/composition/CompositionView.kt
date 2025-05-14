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
import kotlin.math.roundToInt

class CompositionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    enum class DragMode {
        TOP_START, TOP_CENTER, TOP_END,
        CENTER_START, CENTER_END,
        BOTTOM_START, BOTTOM_CENTER, BOTTOM_END,
        FRAME_MOVE
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
    private var cropRect = Rect()

    private var lastX = 0f
    private var lastY = 0f

    private var originalSecondaryBitmap: Bitmap? = null
    private var originalSecondaryCanvas: Canvas? = null
    private var rotatedSecondaryBitmap: Bitmap? = null
    private var rotatedSecondaryCanvas: Canvas? = null

    private var rotationDegrees = 0
    private var scaleX = 1f
    private var scaleY = 1f

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

    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = gridLineWidth.toFloat()
        color = resources.getColor(R.color.white, null)
    }

    var cropMode = CROP_FREEFORM
        set(value) {
            field = value
            when (value) {
                CROP_ORIGINAL -> {
                    if (imageHeight != 0) {
                        cropRatio = imageWidth.toFloat() / imageHeight.toFloat()
                    }
                }
                CROP_1_1 -> {
                    cropRatio = 1f
                }
                CROP_2_3 -> {
                    cropRatio = 2 / 3f
                }
                CROP_3_2 -> {
                    cropRatio = 3 / 2f
                }
                CROP_3_4 -> {
                    cropRatio = 3 / 4f
                }
                CROP_4_3 -> {
                    cropRatio = 4 / 3f
                }
                CROP_9_16 -> {
                    cropRatio = 9 / 16f
                }
                CROP_16_9 -> {
                    cropRatio = 16 / 9f
                }
            }
            invalidate()
        }

    private var cropRatio: Float? = null
        set(value) {
            field = value
            onCropRatioChanged(value)
        }

    private var dragMode: DragMode? = null

    private val handleSize = dpToPx(48f)

    private val minCropRectSize = dpToPx(56f)

    private val gridLineWidth = dpToPx(1f)

    private var needResetCropRect = true

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

                MotionEvent.ACTION_MOVE -> {
                    onActionMove(event)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragMode = null
                }
            }
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val left = cropRect.left.toFloat()
        val top = cropRect.top.toFloat()
        val right = cropRect.right.toFloat()
        val bottom = cropRect.bottom.toFloat()
        val centerX = cropRect.exactCenterX()
        val centerY = cropRect.exactCenterY()
        when {
            x >= left && x <= left + handleSize && y >= top && y <= top + handleSize -> {
                dragMode = DragMode.TOP_START
            }

            x >= centerX - handleSize / 2 && x <= centerX + handleSize / 2 && y >= top - handleSize && y <= top + handleSize -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.TOP_CENTER else null
            }

            x >= right - handleSize && x <= right && y >= top && y <= top + handleSize -> {
                dragMode = DragMode.TOP_END
            }

            x >= left - handleSize && x <= left + handleSize && y >= centerY - handleSize / 2 && y <= centerY + handleSize / 2 -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.CENTER_START else null
            }

            x >= right - handleSize && x <= right + handleSize && y >= centerY - handleSize / 2 && y <= centerY + handleSize / 2 -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.CENTER_END else null
            }

            x >= left && x <= left + handleSize && y >= bottom - handleSize && y <= bottom -> {
                dragMode = DragMode.BOTTOM_START
            }

            x >= centerX - handleSize / 2 && x <= centerX + handleSize / 2 && y >= bottom - handleSize && y <= bottom + handleSize -> {
                dragMode = if (cropMode == CROP_FREEFORM) DragMode.BOTTOM_CENTER else null
            }

            x >= right - handleSize && x <= right && y >= bottom - handleSize && y <= bottom -> {
                dragMode = DragMode.BOTTOM_END
            }

            else -> {
                dragMode = DragMode.FRAME_MOVE
            }
        }
        lastX = event.x
        lastY = event.y
        LogUtil.d(TAG, "dragMode=$dragMode, lastY=$lastY")
    }

    private fun onActionMove(event: MotionEvent) {
        if (cropMode == CROP_FREEFORM) {
            when (dragMode) {
                DragMode.TOP_CENTER -> {
                    val deltaY = event.y - lastY
                    cropRect.top = (cropRect.top + deltaY).coerceIn(imageRect.top.toFloat(), cropRect.bottom.toFloat() - minCropRectSize).toInt()
                    lastY = event.y
                    LogUtil.d(TAG, "cropRect=$cropRect, eventY=${event.y}, lastY=$lastY, deltaY=$deltaY")
                    invalidate()
                }

                DragMode.BOTTOM_CENTER -> {
                    val deltaY = event.y - lastY
                    cropRect.bottom = (cropRect.bottom + deltaY).coerceIn(cropRect.top.toFloat() + minCropRectSize, imageRect.bottom.toFloat()).toInt()
                    lastY = event.y
                    invalidate()
                }

                DragMode.CENTER_START -> {
                    val deltaX = event.x - lastX
                    cropRect.left = (cropRect.left + deltaX).coerceIn(imageRect.left.toFloat(), cropRect.right.toFloat() - minCropRectSize).toInt()
                    lastX = event.x
                    invalidate()
                }

                DragMode.CENTER_END -> {
                    val deltaX = event.x - lastX
                    cropRect.right = (cropRect.right + deltaX).coerceIn(cropRect.left.toFloat() + minCropRectSize, imageRect.right.toFloat()).toInt()
                    lastX = event.x
                    invalidate()
                }

                DragMode.TOP_START -> {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    cropRect.left = (cropRect.left + deltaX).coerceIn(imageRect.left.toFloat(), cropRect.right.toFloat() - minCropRectSize).toInt()
                    cropRect.top = (cropRect.top + deltaY).coerceIn(imageRect.top.toFloat(), cropRect.bottom.toFloat() - minCropRectSize).toInt()
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }

                DragMode.TOP_END -> {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    cropRect.right = (cropRect.right + deltaX).coerceIn(cropRect.left.toFloat() + minCropRectSize, imageRect.right.toFloat()).toInt()
                    cropRect.top = (cropRect.top + deltaY).coerceIn(imageRect.top.toFloat(), cropRect.bottom.toFloat() - minCropRectSize).toInt()
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }

                DragMode.BOTTOM_START -> {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    cropRect.left = (cropRect.left + deltaX).coerceIn(imageRect.left.toFloat(), cropRect.right.toFloat() - minCropRectSize).toInt()
                    cropRect.bottom = (cropRect.bottom + deltaY).coerceIn(cropRect.top.toFloat() + minCropRectSize, imageRect.bottom.toFloat()).toInt()
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }

                DragMode.BOTTOM_END -> {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY
                    cropRect.right = (cropRect.right + deltaX).coerceIn(cropRect.left.toFloat() + minCropRectSize, imageRect.right.toFloat()).toInt()
                    cropRect.bottom = (cropRect.bottom + deltaY).coerceIn(cropRect.top.toFloat() + minCropRectSize, imageRect.bottom.toFloat()).toInt()
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                }

                else -> {}
            }
        } else if (cropRatio != null) {
            when (dragMode) {
                DragMode.TOP_START -> {
                    val deltaX = event.x - lastX
                    val deltaY = deltaX / cropRatio!!
                    val newLeft = (cropRect.left + deltaX).roundToInt()
                    val leftCoerce = newLeft.coerceIn(imageRect.left, cropRect.right - minCropRectSize)
                    val newTop = (cropRect.top + deltaY).roundToInt()
                    val topCoerce = newTop.coerceIn(imageRect.top, cropRect.bottom - minCropRectSize)
                    if (newLeft == leftCoerce && newTop == topCoerce) {
                        cropRect.left = leftCoerce
                        cropRect.top = topCoerce
                        lastX = event.x
                        lastY = event.y
                        invalidate()
                    }
                }

                DragMode.TOP_END -> {
                    val deltaX = event.x - lastX
                    val deltaY = -deltaX / cropRatio!!
                    val newRight = (cropRect.right + deltaX).roundToInt()
                    val rightCoerce = newRight.coerceIn(cropRect.left + minCropRectSize, imageRect.right)
                    val newTop = (cropRect.top + deltaY).roundToInt()
                    val topCoerce = newTop.coerceIn(imageRect.top, cropRect.bottom - minCropRectSize)
                    if (newRight == rightCoerce && newTop == topCoerce) {
                        cropRect.right = rightCoerce
                        cropRect.top = topCoerce
                        lastX = event.x
                        lastY = event.y
                        invalidate()
                    }
                }

                DragMode.BOTTOM_START -> {
                    val deltaX = event.x - lastX
                    val deltaY = -deltaX / cropRatio!!
                    val newLeft = (cropRect.left + deltaX).roundToInt()
                    val leftCoerce = newLeft.coerceIn(imageRect.left, cropRect.right - minCropRectSize)
                    val newBottom = (cropRect.bottom + deltaY).roundToInt()
                    val bottomCoerce = newBottom.coerceIn(cropRect.top + minCropRectSize, imageRect.bottom)
                    if (newLeft == leftCoerce && newBottom == bottomCoerce) {
                        cropRect.left = leftCoerce
                        cropRect.bottom = bottomCoerce
                        lastX = event.x
                        lastY = event.y
                        invalidate()
                    }
                }

                DragMode.BOTTOM_END -> {
                    val deltaX = event.x - lastX
                    val deltaY = deltaX / cropRatio!!
                    val newRight = (cropRect.right + deltaX).roundToInt()
                    val rightCoerce = newRight.coerceIn(cropRect.left + minCropRectSize, imageRect.right)
                    val newBottom = (cropRect.bottom + deltaY).roundToInt()
                    val bottomCoerce = newBottom.coerceIn(cropRect.top + minCropRectSize, imageRect.bottom)
                    if (newRight == rightCoerce && newBottom == bottomCoerce) {
                        cropRect.right = rightCoerce
                        cropRect.bottom = bottomCoerce
                        lastX = event.x
                        lastY = event.y
                        invalidate()
                    }
                }

                else -> {}
            }
        }
        if (dragMode == DragMode.FRAME_MOVE) {
            val deltaX = event.x - lastX
            val deltaY = event.y - lastY
            if (cropRect.left + deltaX >= imageRect.left && cropRect.right + deltaX <= imageRect.right) {
                cropRect.left = (cropRect.left + deltaX).toInt()
                cropRect.right = (cropRect.right + deltaX).toInt()
            }
            if (cropRect.top + deltaY >= imageRect.top && cropRect.bottom + deltaY <= imageRect.bottom) {
                cropRect.top = (cropRect.top + deltaY).toInt()
                cropRect.bottom = (cropRect.bottom + deltaY).toInt()
            }
            lastX = event.x
            lastY = event.y
            invalidate()
        }
    }

    private fun onCropRatioChanged(ratio: Float?) {
        val imageRatio = if (rotationDegrees % 180 == 0) {
            imageWidth.toFloat() / imageHeight.toFloat()
        } else {
            imageHeight.toFloat() / imageWidth.toFloat()
        }
        if (ratio != null) {
            if (ratio >= imageRatio) {
                cropRect.left = imageRect.left
                cropRect.right = imageRect.right
                val height = (imageRect.width() / ratio).toInt()
                cropRect.top = (imageRect.centerY() - height / 2)
                cropRect.bottom = (imageRect.centerY() + height / 2)
            } else {
                cropRect.top = imageRect.top
                cropRect.bottom = imageRect.bottom
                val width = (imageRect.height() * ratio).toInt()
                cropRect.left = (imageRect.centerX() - width / 2)
                cropRect.right = (imageRect.centerX() + width / 2)
            }
        }
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

    private fun transformImageOnSecondaryCanvas() {
        val imageBitmap = imageBitmap ?: return

        originalSecondaryCanvas?.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        rotatedSecondaryCanvas?.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)

        val centerX = imageBitmap.width / 2f
        val centerY = imageBitmap.height / 2f

        if (rotationDegrees % 180 == 0) {
            val tempMatrix = Matrix()
            tempMatrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)
            tempMatrix.postScale(scaleX, scaleY, centerX, centerY)
            originalSecondaryCanvas?.drawBitmap(imageBitmap, tempMatrix, null)
        } else {
            val tempMatrix = Matrix()
            tempMatrix.postRotate(rotationDegrees.toFloat(), centerX, centerY)
            tempMatrix.postScale(scaleY, scaleX, centerX, centerY)
            tempMatrix.postTranslate((imageBitmap.height - imageBitmap.width) / 2f, (imageBitmap.width - imageBitmap.height) / 2f)
            rotatedSecondaryCanvas?.drawBitmap(imageBitmap, tempMatrix, null)
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageBitmap = bitmap
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        createSecondaryCanvas()
        if (cropMode == CROP_ORIGINAL) {
            cropRatio = imageWidth.toFloat() / imageHeight.toFloat()
        }
        LogUtil.d(TAG, "setImageBitmap: imageWidth=$imageWidth, imageHeight=$imageHeight")
        invalidate()
    }

    fun rotateLeft() {
        rotationDegrees -= 90
        needResetCropRect = true
        invalidate()
    }

    fun rotateRight() {
        rotationDegrees += 90
        needResetCropRect = true
        invalidate()
    }

    fun mirror() {
        scaleX = -scaleX
        needResetCropRect = true
        invalidate()
    }

    fun flip() {
        scaleY = -scaleY
        needResetCropRect = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawImage(canvas)
        if (dragMode != null) {
            drawGrids(canvas)
        }
        drawBorder(canvas)
        drawCornerHandles(canvas)
        if (cropMode == CROP_FREEFORM) {
            drawMidHandles(canvas)
        }
    }

    private fun drawImage(canvas: Canvas) {
        transformImageOnSecondaryCanvas()

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
        if (needResetCropRect) {
            cropRect.set(left, top, left + scaledWidth, top + scaledHeight)
            needResetCropRect = false
        }

        getBitmapToDraw()?.let {
            canvas.drawBitmap(
                it,
                Rect(0, 0, it.width, it.height),
                imageRect,
                null
            )
        }

        // Draw mask
        val saveCount = canvas.save()
        canvas.clipRect(imageRect)
        canvas.clipOutRect(cropRect)
        canvas.drawColor(resources.getColor(R.color.black_50p, null))
        canvas.restoreToCount(saveCount)
    }

    private fun getBitmapToDraw() = if (rotationDegrees % 180 == 0) originalSecondaryBitmap else rotatedSecondaryBitmap

    fun getResultBitmap(): Bitmap? {
        return getBitmapToDraw()?.let { sourceBitmap ->
            val sourceRect = Rect()
            val bitmapWidth = sourceBitmap.width
            val bitmapHeight = sourceBitmap.height
            
            val scaleX = bitmapWidth.toFloat() / imageRect.width()
            val scaleY = bitmapHeight.toFloat() / imageRect.height()
            
            sourceRect.left = ((cropRect.left - imageRect.left) * scaleX).toInt()
            sourceRect.top = ((cropRect.top - imageRect.top) * scaleY).toInt()
            sourceRect.right = sourceRect.left + (cropRect.width() * scaleX).toInt()
            sourceRect.bottom = sourceRect.top + (cropRect.height() * scaleY).toInt()
            
            sourceRect.left = sourceRect.left.coerceIn(0, bitmapWidth)
            sourceRect.top = sourceRect.top.coerceIn(0, bitmapHeight)
            sourceRect.right = sourceRect.right.coerceIn(0, bitmapWidth)
            sourceRect.bottom = sourceRect.bottom.coerceIn(0, bitmapHeight)
            
            try {
                Bitmap.createBitmap(
                    sourceBitmap,
                    sourceRect.left,
                    sourceRect.top,
                    sourceRect.width(),
                    sourceRect.height()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun drawGrids(canvas: Canvas) {
        canvas.drawLine(
            cropRect.left + (1 / 3f) * cropRect.width() - gridLineWidth / 2f,
            cropRect.top.toFloat(),
            cropRect.left + (1 / 3f) * cropRect.width() - gridLineWidth / 2f,
            cropRect.bottom.toFloat(),
            gridLinePaint
        )
        canvas.drawLine(
            cropRect.left + (2 / 3f) * cropRect.width() - gridLineWidth / 2f,
            cropRect.top.toFloat(),
            cropRect.left + (2 / 3f) * cropRect.width() - gridLineWidth / 2f,
            cropRect.bottom.toFloat(),
            gridLinePaint
        )
        canvas.drawLine(
            cropRect.left.toFloat(),
            cropRect.top + (1 / 3f) * cropRect.height() - gridLineWidth / 2f,
            cropRect.right.toFloat(),
            cropRect.top + (1 / 3f) * cropRect.height() - gridLineWidth / 2f,
            gridLinePaint
        )
        canvas.drawLine(
            cropRect.left.toFloat(),
            cropRect.top + (2 / 3f) * cropRect.height() - gridLineWidth / 2f,
            cropRect.right.toFloat(),
            cropRect.top + (2 / 3f) * cropRect.height() - gridLineWidth / 2f,
            gridLinePaint
        )
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawRect(
            cropRect.left.toFloat() + borderPaint.strokeWidth / 2,
            cropRect.top.toFloat() + borderPaint.strokeWidth / 2,
            cropRect.right.toFloat() - borderPaint.strokeWidth / 2,
            cropRect.bottom.toFloat() - borderPaint.strokeWidth / 2,
            borderPaint
        )
    }

    private fun drawCornerHandles(canvas: Canvas) {
        val strokeWidth = handlePaint.strokeWidth

        val left = cropRect.left.toFloat() + strokeWidth / 2
        val top = cropRect.top.toFloat() + strokeWidth / 2
        val right = cropRect.right.toFloat() - strokeWidth / 2
        val bottom = cropRect.bottom.toFloat() - strokeWidth / 2

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

        val centerX = (cropRect.left.toFloat() + cropRect.right.toFloat()) / 2
        val centerY = (cropRect.top.toFloat() + cropRect.bottom.toFloat()) / 2
        val top = cropRect.top.toFloat() + strokeWidth / 2
        val left = cropRect.left.toFloat() + strokeWidth / 2
        val right = cropRect.right.toFloat() - strokeWidth / 2
        val bottom = cropRect.bottom.toFloat() - strokeWidth / 2

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