package me.squeezymo.demo.ui.util

import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.animation.doOnEnd
import me.squeezymo.demo.ui.widget.TooltipWidget
import me.squeezymo.spotlight.overlay.AccentParams
import me.squeezymo.spotlight.overlay.SpotlightController
import me.squeezymo.spotlight.overlay.Tooltip
import me.squeezymo.spotlight.overlay.internal.view.TooltipsViewGroup

fun SpotlightController.showTooltip(
    anchor: View,
    accentShape: AccentParams.Shape,
    tooltipText: String,
    @Px distancePx: Int,
    onTooltipShown: ((tooltip: View) -> Unit)? = null,
    onAcknowledged: ((TooltipWidget) -> Unit)? = { hide() },
    invalidateAccentOnRedraws: Boolean = accentShape is AccentParams.Shape.ExactViewShape
) {
    show(
        srcView = anchor,
        accents = mapOf(
            anchor to AccentParams(
                accentShape = accentShape,
                invalidateAccentOnRedraws = invalidateAccentOnRedraws,
                tooltip = createTooltip(
                    anchor = anchor,
                    text = tooltipText,
                    distancePx = distancePx,
                    onAcknowledged = onAcknowledged,
                    onTooltipShown = onTooltipShown
                )
            )
        )
    )
}

fun SpotlightController.createTooltip(
    anchor: View,
    text: String,
    distancePx: Int,
    @FloatRange(from = 0.0, to = 360.0) manualBearing: Float? = null,
    showArrow: Boolean = true,
    onTooltipShown: ((tooltip: View) -> Unit)? = null,
    onAcknowledged: ((TooltipWidget) -> Unit)? = null
): Tooltip {
    val bearing = manualBearing ?: TooltipsViewGroup.LayoutParams.suggestBearing(
        spotlightController = this,
        anchor = anchor
    )
    val bearingReference = TooltipsViewGroup.LayoutParams.suggestBearingReference(bearing)

    return Tooltip(
        view = TooltipWidget.create(
            context = anchor.context,
            text = text,
            showArrow = showArrow,
            tooltipBearing = bearing,
            onAcknowledged = onAcknowledged
        ),
        layoutParams = TooltipsViewGroup.LayoutParams.createRelativeToAnchor(
            width = ViewGroup.LayoutParams.WRAP_CONTENT,
            height = ViewGroup.LayoutParams.WRAP_CONTENT,
            anchor = anchor,
            distanceFromAnchorPx = distancePx,
            bearing = bearing,
            bearingReference = bearingReference,
            allowAnchorTooltipOverlap = false,
            hasFixedPosition = false
        ),
        onTooltipReadyToShow = { tooltip: View ->
            onTooltipReadyToShow(tooltip, onTooltipShown)
        }
    )
}

private fun onTooltipReadyToShow(
    tooltipView: View,
    onTooltipShown: ((tooltip: View) -> Unit)?
) {
    tooltipView.visibility = View.VISIBLE

    if (tooltipView is TooltipWidget) {
        tooltipView
            .createInAnimator()
            .apply {
                doOnEnd {
                    onTooltipShown?.invoke(tooltipView)
                }
            }
            .start()
    } else {
        onTooltipShown?.invoke(tooltipView)
    }
}
