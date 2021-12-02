package me.squeezymo.spotlight.overlay

import android.animation.Animator
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import me.squeezymo.spotlight.overlay.ext.mapNotNullValues
import me.squeezymo.spotlight.overlay.internal.view.DimmingOverlayView
import me.squeezymo.spotlight.overlay.internal.view.TooltipsViewGroup

class SpotlightController internal constructor(
    internal val parent: ViewGroup
) {

    /**
     * Animator creator object that will be used by default.
     */
    var defaultAnimatorCreator: DimmingOverlayViewAnimatorCreator? = CircularRevealAnimatorCreator()

    /**
     * Color of the dimming layer.
     */
    @ColorInt
    var dimmingColor: Int = Color.parseColor("#B0000000")
        set(value) {
            dimmingOverlayView?.setDimmingColor(value)
            field = value
        }

    private var dimmingOverlayView: DimmingOverlayView? = null
    private var tooltipsViewGroup: TooltipsViewGroup? = null

    private var touchHandlingStrategy: OverlayTouchHandlingStrategy? = null

    private var inAnimator: Animator? = null

    fun isShown(): Boolean {
        return dimmingOverlayView?.visibility == View.VISIBLE
    }

    /**
     *
     * @param srcView View that caused this overlay to appear. May be used by animator to focus user's
     * attention. If null, there is no specific view to highlight during animation.
     * @param accents Views that are not dimmed by dimming overlay.
     * @param dimmingOverlayViewAnimatorCreator Used to define animation to play during the dimming view appearance.
     * If null, no animation will be played.
     */
    fun show(
        srcView: View? = null,
        accents: Map<View, AccentParams> =
            if (srcView == null) emptyMap() else mapOf(srcView to AccentParams()),
        dimmingOverlayViewAnimatorCreator: DimmingOverlayViewAnimatorCreator? = defaultAnimatorCreator
    ) {
        if (isShown()) {
            update(accents)
            return
        }

        val dimmingView = getOrCreateDimmingView()

        updateAccents(accents, false)

        fun doBeforeShowingOverlay() {
            dimmingView.visibility = View.VISIBLE
        }

        fun doAfterShowingOverlay() {
            val tooltipViews = updateTooltips(accents)
            tooltipsViewGroup?.visibility = View.VISIBLE

            tooltipViews.forEach { tooltipView ->
                tooltipView.onTooltipReadyToShow(tooltipView.view)
            }

            inAnimator = null
        }

        inAnimator?.cancel()
        inAnimator = dimmingOverlayViewAnimatorCreator?.createInAnimator(
            parent,
            dimmingView,
            srcView,
            accents[srcView]
        ).also { animator ->
            if (animator == null) {
                doBeforeShowingOverlay()
                doAfterShowingOverlay()
            } else {
                animator
                    .apply {
                        doOnStart {
                            doBeforeShowingOverlay()
                        }
                        doOnCancel {
                            doAfterShowingOverlay()
                        }
                        doOnEnd {
                            doAfterShowingOverlay()
                        }
                    }
                    .start()
            }
        }
    }

    fun update(
        accents: Map<View, AccentParams> = emptyMap()
    ) {
        if (isShown()) {
            inAnimator?.cancel()

            updateAccents(accents, true)

            val tooltipViews = updateTooltips(accents)
            tooltipsViewGroup?.visibility = View.VISIBLE

            tooltipViews.forEach { tooltipView ->
                tooltipView.onTooltipReadyToShow(tooltipView.view)
            }

        } else {
            show(
                accents = accents
            )
        }
    }

    fun hide() {
        inAnimator?.cancel()

        removeDimmingView()
        removeTooltipsViewGroup()
    }

    fun findTooltipByAnchor(anchor: View): View? {
        return tooltipsViewGroup
            ?.children
            ?.find { view ->
                val lparams =
                    view.layoutParams as? TooltipsViewGroup.LayoutParams ?: return@find false
                lparams.anchor.anchorViewRef.get() === anchor
            }
    }

    private fun updateAccents(
        accents: Map<View, AccentParams>,
        invalidateImmediately: Boolean
    ) {
        getOrCreateDimmingView().setAccents(accents, invalidateImmediately)
    }

    private fun updateTooltips(
        accents: Map<View, AccentParams>
    ): Collection<Tooltip> {
        val views = accents
            .mapNotNullValues { _, params ->
                params.tooltip
            }

        if (views.isEmpty()) {
            removeTooltipsViewGroup()
        } else {
            val tooltipsViewGroup = getOrCreateTooltipsViewGroup()
            tooltipsViewGroup.removeAllViews()

            for ((_, tooltip) in views) {
                tooltip.view.layoutParams = tooltip.layoutParams
                tooltipsViewGroup.addView(tooltip.view)
            }
        }

        return views.values
    }

    fun setTouchHandlingStrategy(strategy: OverlayTouchHandlingStrategy) {
        touchHandlingStrategy = strategy
        dimmingOverlayView?.setOverlayOnTouchListener(strategy.listener)
    }

    private fun getOrCreateDimmingView(): DimmingOverlayView {
        return dimmingOverlayView ?: addDimmingView()
    }

    private fun addDimmingView(): DimmingOverlayView {
        return DimmingOverlayView(parent.context).also { newDimmingView ->
            newDimmingView.id = View.generateViewId()
            newDimmingView.setDimmingColor(dimmingColor)
            newDimmingView.visibility = View.INVISIBLE

            dimmingOverlayView = newDimmingView
            parent.addView(newDimmingView)

            touchHandlingStrategy?.let(::setTouchHandlingStrategy)
        }
    }

    private fun removeDimmingView() {
        dimmingOverlayView?.let { dimmingView ->
            parent.removeView(dimmingView)
            dimmingView.clear()
            this.dimmingOverlayView = null
        }
    }

    private fun getOrCreateTooltipsViewGroup(): TooltipsViewGroup {
        return tooltipsViewGroup ?: addTooltipsViewGroup()
    }

    private fun addTooltipsViewGroup(): TooltipsViewGroup {
        return TooltipsViewGroup(parent.context).also { newTooltipsView ->
            newTooltipsView.id = View.generateViewId()
            newTooltipsView.visibility = View.INVISIBLE

            tooltipsViewGroup = newTooltipsView
            parent.addView(newTooltipsView)
        }
    }

    private fun removeTooltipsViewGroup() {
        tooltipsViewGroup?.let {
            parent.removeView(it)
            it.removeAllViews()
            tooltipsViewGroup = null
        }
    }

}
