package com.tgwgroup.zhoupics.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.LayoutBidirectionalSliderBinding
import com.tgwgroup.zhoupics.utils.LogUtil
import kotlin.math.abs
import kotlin.math.roundToInt

class BidirectionalSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val TAG = "BidirectionalSlider"
    }

    private val binding = LayoutBidirectionalSliderBinding.inflate(LayoutInflater.from(context), this)

    private var isBidirectional = false
    private var progress = 0f
    private var minValue = 0f
    private var maxValue = 1f
    private var listener: OnProgressChangeListener? = null
    private var bubble: SliderBubble? = null

    interface OnProgressChangeListener {
        fun onStartTrackingTouch()
        fun onStopTrackingTouch()
        fun onProgressChanged(progress: Float, fromUser: Boolean)
    }

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BidirectionalSlider)
            isBidirectional = typedArray.getBoolean(R.styleable.BidirectionalSlider_bidirectional, false)
            minValue = typedArray.getFloat(R.styleable.BidirectionalSlider_minValue, 0f)
            maxValue = typedArray.getFloat(R.styleable.BidirectionalSlider_maxValue, 1f)
            val defaultProgress = typedArray.getFloat(R.styleable.BidirectionalSlider_defaultProgress, 0f)
            typedArray.recycle()

            if (isBidirectional) {
                val normalizedProgress = 2f * (defaultProgress - minValue) / (maxValue - minValue) - 1f
                progress = normalizedProgress.coerceIn(-1f, 1f)
            } else {
                val normalizedProgress = (defaultProgress - minValue) / (maxValue - minValue)
                progress = normalizedProgress.coerceIn(0f, 1f)
            }
        }
        setProgress(progress, false)
        setupTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        setOnTouchListener { _, event ->
            if (!isEnabled) {
                LogUtil.d(TAG, "isEnabled == false")
                return@setOnTouchListener false
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    LogUtil.d(TAG, "ACTION_DOWN")
                    listener?.onStartTrackingTouch()
                    setProgressFromEvent(event)
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    setProgressFromEvent(event)
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    LogUtil.d(TAG, "ACTION_UP / ACTION_CANCEL")
                    listener?.onStopTrackingTouch()
                    bubble?.isInvisible = true
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun setProgressFromEvent(event: MotionEvent) {
        val x = event.x
        val thumbWidth = binding.vThumb.width
        val width = width - thumbWidth
        var percent: Float

        if (isBidirectional) {
            val halfWidth = width / 2
            percent = (x - thumbWidth / 2 - halfWidth) / halfWidth
            percent = percent.coerceIn(-1f, 1f)
        } else {
            percent = (x - thumbWidth / 2) / width
            percent = percent.coerceIn(0f, 1f)
        }

        setProgress(percent, true)
    }

    fun bindBubble(bubble: SliderBubble) {
        this.bubble = bubble
    }

    fun setValue(value: Float) {
        val normalizedValue: Float
        if (isBidirectional) {
            normalizedValue = 2f * (value - minValue) / (maxValue - minValue) - 1f
        } else {
            normalizedValue = (value - minValue) / (maxValue - minValue)
        }
        setProgress(normalizedValue)
    }

    fun getValue(): Float {
        return if (isBidirectional) {
            minValue + (maxValue - minValue) * (progress + 1f) / 2f
        } else {
            minValue + (maxValue - minValue) * progress
        }
    }

    private fun setProgress(progress: Float) {
        setProgress(progress, false)
    }

    fun setValueRange(min: Float, max: Float) {
        if (min >= max) {
            throw IllegalArgumentException("min must be less than max")
        }
        val currentValue = getValue()
        minValue = min
        maxValue = max
        setValue(currentValue)
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener) {
        this.listener = listener
    }

    fun setBidirectional(bidirectional: Boolean) {
        if (bidirectional != isBidirectional) {
            isBidirectional = bidirectional
            val currentValue = getValue()
            if (bidirectional) {
                setProgress(0f)
            } else {
                if (progress < 0) {
                    setProgress(0f)
                }
            }
            setValue(currentValue)
        }
    }

    private fun setProgress(progress: Float, fromUser: Boolean) {
        if (isBidirectional) {
            this.progress = progress.coerceIn(-1f, 1f)
        } else {
            this.progress = progress.coerceIn(0f, 1f)
        }

        updateProgressBar(fromUser)
        if (isBidirectional) {
            val mappedValue = minValue + (maxValue - minValue) * (this.progress + 1f) / 2f
            listener?.onProgressChanged(mappedValue, fromUser)
            bubble?.updateProgressText(mappedValue)
        } else {
            val mappedValue = minValue + (maxValue - minValue) * this.progress
            listener?.onProgressChanged(mappedValue, fromUser)
            bubble?.updateProgressText(mappedValue)
        }
    }

    private fun updateProgressBar(fromUser: Boolean) {
        LogUtil.d(TAG, "updateProgressBar fromUser=$fromUser progress=$progress")
        val thumbWidth = binding.vThumb.width
        val realWidth = width - thumbWidth
        val thumbCenter = thumbWidth / 2f

        if (isBidirectional) {
            val centerX = realWidth / 2f + thumbCenter
            val thumbX = centerX + progress * (realWidth / 2f)

            binding.vThumb.x = thumbX - thumbCenter
            bubble?.updatePositionX(thumbX)

            if (progress > 0) {
                binding.vTrackProgress.x = centerX
                binding.vTrackProgress.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = (progress * realWidth / 2f).roundToInt()
                }
            } else {
                val progressWidth = abs(progress) * realWidth / 2f
                binding.vTrackProgress.x = centerX - progressWidth
                binding.vTrackProgress.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = progressWidth.roundToInt()
                }
            }
        } else {
            val thumbX = realWidth * progress + thumbCenter
            binding.vThumb.x = thumbX - thumbCenter
            bubble?.updatePositionX(thumbX)
            binding.vTrackProgress.x = 0f
            binding.vTrackProgress.updateLayoutParams<ViewGroup.LayoutParams> {
                width = thumbX.roundToInt()
            }
        }

        binding.vTrackProgress.requestLayout()
        if (fromUser) {
            bubble?.isInvisible = false
        }
    }
}