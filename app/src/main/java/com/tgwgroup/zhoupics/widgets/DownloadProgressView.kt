package com.tgwgroup.zhoupics.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.utils.dpToPx
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

class DownloadProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var progressBitmap: Bitmap? = null
    private var progressCanvas: Canvas? = null

    var pixelSize: Int = 4
        set(value) {
            field = value.coerceAtLeast(1)
            updateBitmap()
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            updateBitmap()
            invalidate()
        }

    @ColorInt
    var fillColor: Int = context.getColor(R.color.filter_color_brown_light)
        set(value) {
            field = value
            fillPaint.color = value
            updateBitmap()
            invalidate()
        }

    @ColorInt
    var bgColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            backgroundPaint.color = value
            updateBitmap()
            invalidate()
        }

    init {
        fillPaint.color = fillColor
        backgroundPaint.color = bgColor

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DownloadProgressView, defStyleAttr, 0)
        try {
            progress = typedArray.getFloat(R.styleable.DownloadProgressView_progress, 0f)
            fillColor = typedArray.getColor(R.styleable.DownloadProgressView_fillColor, context.getColor(R.color.filter_color_brown_light))
            bgColor = typedArray.getColor(R.styleable.DownloadProgressView_backgroundColor, Color.TRANSPARENT)
            pixelSize = typedArray.getInt(R.styleable.DownloadProgressView_pixelSize, 4)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            progressBitmap?.recycle()
            progressBitmap = createBitmap(w, h)
            progressCanvas = Canvas(progressBitmap!!)
            updateBitmap()
        }
    }

    private fun updateBitmap() {
        val bitmapCanvas = progressCanvas ?: return
        val bmpWidth = width.toFloat()
        val bmpHeight = height.toFloat()

        bitmapCanvas.drawRect(0f, 0f, bmpWidth, bmpHeight, backgroundPaint)

        if (progress == 0f) {
            return
        }

        val gridWidth = (bmpWidth / pixelSize).roundToInt()
        val gridHeight = (bmpHeight / pixelSize).roundToInt()

        val totalBlocks = gridWidth * gridHeight
        val filledBlocks = (totalBlocks * (progress / 100.0f)).roundToInt()

        var currentFilledBlocks = 0
        for (gridY in 0 until gridHeight) {
            for (gridX in 0 until gridWidth) {
                if (currentFilledBlocks < filledBlocks) {
                    val left = (gridX * pixelSize).toFloat()
                    val top = (gridY * pixelSize).toFloat()
                    val right = left + pixelSize
                    val bottom = top + pixelSize

                    bitmapCanvas.drawRect(left, top, right, bottom, fillPaint)
                    currentFilledBlocks++
                } else {
                    break
                }
            }
            if (currentFilledBlocks >= filledBlocks) {
                break
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        progressBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = dpToPx(200f)
        val desiredHeight = dpToPx(200f)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressBitmap?.recycle()
        progressBitmap = null
        progressCanvas = null
    }
}