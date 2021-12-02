package me.squeezymo.demo.ui.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

fun createBeatAnimation(
    view: View,
    maxScale: Float = 1.2F
): Animator {
    return ValueAnimator
        .ofFloat(1F, maxScale, 1F)
        .apply {
            addUpdateListener { valueAnimator ->
                val scale = valueAnimator.animatedValue as Float

                view.scaleX = scale
                view.scaleY = scale
            }

            interpolator = AccelerateDecelerateInterpolator()
            duration = 350L
            repeatCount = 1
        }
}
