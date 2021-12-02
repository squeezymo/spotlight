package me.squeezymo.demo.ui.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SimpleSpacingItemDecoration(
    @RecyclerView.Orientation private val orientation: Int,
    private val spacePx: Int,
    private val spaceBeforeFirstPx: Int? = null,
    private val spaceAfterLastPx: Int? = null
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position =
            parent.getChildLayoutPosition(view).takeIf { it != RecyclerView.NO_POSITION } ?: return
        val lastPosition = state.itemCount - 1

        if (position == 0) {
            if (spaceBeforeFirstPx == null) {
                outRect.before = spacePx / 2
            } else {
                outRect.before = spaceBeforeFirstPx
            }

            if (position != lastPosition) {
                outRect.after = spacePx / 2
            }
        }

        if (position == lastPosition) {
            if (spaceAfterLastPx == null) {
                outRect.after = spacePx / 2
            } else {
                outRect.after = spaceAfterLastPx
            }

            if (position != 0) {
                outRect.before = spacePx / 2
            }
        }

        if (position != 0 && position != lastPosition) {
            outRect.before = spacePx / 2
            outRect.after = spacePx / 2
        }
    }

    private var Rect.before: Int
        get() {
            return when (orientation) {
                RecyclerView.VERTICAL -> top
                RecyclerView.HORIZONTAL -> left
                else -> throw IllegalStateException("Unsupported orientation: $orientation")
            }
        }
        set(value) {
            when (orientation) {
                RecyclerView.VERTICAL -> top = value
                RecyclerView.HORIZONTAL -> left = value
                else -> throw IllegalStateException("Unsupported orientation: $orientation")
            }
        }

    private var Rect.after: Int
        get() {
            return when (orientation) {
                RecyclerView.VERTICAL -> bottom
                RecyclerView.HORIZONTAL -> right
                else -> throw IllegalStateException("Unsupported orientation: $orientation")
            }
        }
        set(value) {
            when (orientation) {
                RecyclerView.VERTICAL -> bottom = value
                RecyclerView.HORIZONTAL -> right = value
                else -> throw IllegalStateException("Unsupported orientation: $orientation")
            }
        }

}
