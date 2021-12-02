package me.squeezymo.spotlight.overlay

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.annotation.CallSuper
import me.squeezymo.spotlight.overlay.ext.offsetRectToAncestorCoords
import kotlin.math.hypot

interface DimmingOverlayViewAnimatorCreator {

    /**
     * @param parent Parent view group of the overlay.
     * @param dimmingView The dimming overlay view.
     * @param srcView View that caused the overlay to appear. May be used by animation
     * to draw user's attention.
     * @param params Accent params of the srcView.
     *
     * @return Animator object to use or null if overlay must appear instantly.
     */
    fun createInAnimator(
        parent: ViewGroup,
        dimmingView: View,
        srcView: View?,
        params: AccentParams?
    ): Animator?

    /**
     * @param parent Parent view group of the overlay.
     * @param dimmingView The dimming overlay view.
     * @param srcView View that caused the overlay to disappear. May be used by animation
     * to draw user's attention.
     * @param params Accent params of the srcView.
     *
     * @return Animator object to use or null if overlay must disappear instantly.
     */
    fun createOutAnimator(
        parent: ViewGroup,
        dimmingView: View,
        srcView: View?,
        params: AccentParams?
    ): Animator?

}

/**
 * Default circular reveal animation. May be extended in order to add listeners or adjust animator
 * properties such as duration, etc.
 */
open class CircularRevealAnimatorCreator : DimmingOverlayViewAnimatorCreator {

    companion object {

        private const val DURATION_MILLIS = 400L

    }

    @CallSuper
    override fun createInAnimator(
        parent: ViewGroup,
        dimmingView: View,
        srcView: View?,
        params: AccentParams?
    ): Animator? {
        if (srcView == null) {
            // Circular reveal makes sense only when it has a reference point.
            // Otherwise it's better to skip the animation altogether.
            return null
        }

        val srcViewOffsetRect = srcView.offsetRectToAncestorCoords(parent)
        val scale = params?.accentShape?.scale ?: 1F

        return ViewAnimationUtils.createCircularReveal(
            dimmingView,
            srcViewOffsetRect.left + srcView.width / 2 + srcView.translationX.toInt(),
            srcViewOffsetRect.top + srcView.height / 2 + srcView.translationY.toInt(),
            hypot(scale * srcView.width / 2.0, scale * srcView.height / 2.0).toFloat(),
            hypot(
                (dimmingView.parent as View).width.toDouble(),
                (dimmingView.parent as View).height.toDouble()
            ).toFloat()
        ).apply {
            duration = DURATION_MILLIS
        }
    }

    @CallSuper
    override fun createOutAnimator(
        parent: ViewGroup,
        dimmingView: View,
        srcView: View?,
        params: AccentParams?
    ): Animator? {
        return null
    }

}
