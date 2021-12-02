package me.squeezymo.demo.ui.outlineprovider

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.Px

class RoundedCornersOutlineProvider(
    @Px private val radiusPx: Float
) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(
            0, 0, view.width, view.height, radiusPx
        )
    }

}
