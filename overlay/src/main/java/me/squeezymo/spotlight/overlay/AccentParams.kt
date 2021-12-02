package me.squeezymo.spotlight.overlay

import android.view.View
import androidx.annotation.Px
import me.squeezymo.spotlight.overlay.internal.view.TooltipsViewGroup

class AccentParams(
    val accentShape: Shape = Shape.ExactViewShape(),
    val invalidateAccentOnRedraws: Boolean = false,
    val tooltip: Tooltip? = null
) {

    sealed class Shape {

        abstract val scale: Float

        class ExactViewShape(
            override val scale: Float = 1F
        ) : Shape()

        class Circle(
            override val scale: Float = 1.5F
        ) : Shape()

        class Oval(
            override val scale: Float = 1.5F
        ) : Shape()

        class Rectangle(
            override val scale: Float = 1.5F,
            @Px val roundPx: Int = 0
        ) : Shape()

    }

}

class Tooltip(
    val view: View,
    val layoutParams: TooltipsViewGroup.LayoutParams,
    val onTooltipReadyToShow: (tooltip: View) -> Unit = { it.visibility = View.VISIBLE }
)
