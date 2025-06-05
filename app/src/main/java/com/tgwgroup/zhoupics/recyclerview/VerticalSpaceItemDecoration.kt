package com.tgwgroup.zhoupics.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.utils.dpToPx

class VerticalSpaceItemDecoration(
    private val verticalSpaceDp: Int,
    private val includeTopSpace: Boolean = false,
    private val includeBottomSpace: Boolean = false
) : RecyclerView.ItemDecoration() {

    private var verticalSpacePx: Int = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (verticalSpacePx == 0) {
            verticalSpacePx = dpToPx(verticalSpaceDp.toFloat())
        }

        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        if (position < itemCount - 1) {
            outRect.bottom = verticalSpacePx
        }

        if (includeTopSpace && position == 0) {
            outRect.top = verticalSpacePx
        }

        if (includeBottomSpace && position == itemCount - 1 && itemCount > 0) {
            outRect.bottom = verticalSpacePx
        }
    }
}