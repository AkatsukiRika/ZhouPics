package com.tgwgroup.zhoupics.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import com.tgwgroup.zhoupics.databinding.LayoutSliderBubbleBinding
import com.tgwgroup.zhoupics.utils.dpToPx
import kotlin.math.roundToInt

class SliderBubble @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = LayoutSliderBubbleBinding.inflate(LayoutInflater.from(context), this)

    init {
        layoutParams = LayoutParams(dpToPx(48f), dpToPx(48f))
        isInvisible = true
    }

    fun updatePositionX(x: Float) {
        val bubbleWidth = binding.ivBubble.width
        val xOffset = (bubbleWidth / 2)
        updateLayoutParams<MarginLayoutParams> {
            leftMargin = (x - xOffset).toInt()
        }
    }

    fun updateProgressText(progress: Float) {
        val intValue = progress.roundToInt()
        if (intValue > 0) {
            binding.tvValue.text = "+$intValue"
        } else {
            binding.tvValue.text = intValue.toString()
        }
    }
}