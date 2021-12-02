package me.squeezymo.spotlight.overlay.internal.view

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.view.children
import me.squeezymo.spotlight.overlay.BearingReference
import me.squeezymo.spotlight.overlay.BearingReference.*
import me.squeezymo.spotlight.overlay.SpotlightController
import me.squeezymo.spotlight.overlay.ext.offsetRectToAncestorCoords
import java.lang.ref.WeakReference
import kotlin.math.*

class TooltipsViewGroup(
    context: Context
) : ViewGroup(context) {

    private val accentedViewDrawingRect = Rect()
    private val childPositionRect = Rect()

    private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
        requestLayout()
        true
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        val lparams = params as? LayoutParams
            ?: requireNotNull(child?.layoutParams as? LayoutParams) {
                "All tooltip views must specify ${LayoutParams::class.simpleName} prior to being added to ${TooltipsViewGroup::class.simpleName}"
            }

        require(lparams.anchor.bearingRelativeToAnchor in (0F..360F)) {
            "Angle ${lparams.anchor.bearingRelativeToAnchor} is outside of allowed bounds"
        }

        if (!lparams.hasFixedPosition) {
            val treeObserver = lparams.anchor.anchorViewRef.get()?.viewTreeObserver
            treeObserver?.addOnPreDrawListener(onPreDrawListener)
        }

        super.addView(child, index, params)
    }

    override fun removeView(view: View?) {
        removeAssociatedOnPreDrawListener(view)
        super.removeView(view)
    }

    override fun removeAllViews() {
        children.forEach(::removeAssociatedOnPreDrawListener)
        super.removeAllViews()
    }

    private fun removeAssociatedOnPreDrawListener(child: View?) {
        val lparams = child?.layoutParams as? LayoutParams
        lparams?.anchor?.anchorViewRef?.get()?.viewTreeObserver?.let { vto ->
            if (vto.isAlive) {
                vto.removeOnPreDrawListener(onPreDrawListener)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        children.forEach { child ->
            measureChild(
                child,
                widthMeasureSpec,
                heightMeasureSpec
            )
        }

        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { child ->
            val lparams = child.layoutParams as LayoutParams
            val accentedView = lparams.anchor.anchorViewRef.get()

            if (accentedView != null && accentedView.visibility == VISIBLE) {
                accentedView.offsetRectToAncestorCoords(
                    parent as ViewGroup,
                    accentedViewDrawingRect
                )

                calculateChildPosition(
                    child,
                    accentedView,
                    accentedViewDrawingRect,
                    lparams.anchor.bearingRelativeToAnchor,
                    lparams.anchor.bearingReference,
                    lparams.anchor.distanceFromAnchorPx,
                    lparams.anchor.allowAnchorTooltipOverlap,
                    lparams.adjustToParentBounds,
                    childPositionRect
                )

                child.layout(
                    childPositionRect.left,
                    childPositionRect.top,
                    childPositionRect.right,
                    childPositionRect.bottom
                )
            }
        }
    }

    private fun calculateChildPosition(
        childView: View,
        anchorView: View,
        anchorViewRect: Rect,
        bearingRelativeToAnchor: Float,
        bearingReference: BearingReference,
        distanceFromAnchorPx: Int,
        allowAnchorTooltipOverlap: Boolean,
        adjustToParentBounds: Boolean,
        outRect: Rect
    ) {
        val anchorLeft = anchorViewRect.left + anchorView.translationX.toInt()
        val anchorTop = anchorViewRect.top + anchorView.translationY.toInt()

        val anchorCenterX = anchorLeft + anchorView.width / 2
        val anchorCenterY = anchorTop + anchorView.height / 2

        val degrees = when (bearingRelativeToAnchor) {
            in (0F..90F) -> {
                90 - bearingRelativeToAnchor
            }
            in (90F..180F) -> {
                bearingRelativeToAnchor - 90
            }
            in (180F..270F) -> {
                270 - bearingRelativeToAnchor
            }
            else -> {
                bearingRelativeToAnchor - 270
            }
        }

        val rad = Math.toRadians(degrees.toDouble())
        val dist = calculateTotalDistance(
            childView,
            bearingRelativeToAnchor,
            bearingReference,
            rad,
            anchorViewRect,
            distanceFromAnchorPx,
            allowAnchorTooltipOverlap
        )

        val bearingReferencePointX =
            anchorCenterX + (if (bearingRelativeToAnchor in (0F..180F)) 1 else -1) * dist * cos(rad)
        val bearingReferencePointY =
            anchorCenterY + (if (bearingRelativeToAnchor in (90F..270F)) 1 else -1) * dist * sin(rad)

        val left: Int
        val top: Int
        val right: Int
        val bottom: Int

        when (bearingReference) {
            TOP_LEFT -> {
                left = bearingReferencePointX.toInt()
                top = bearingReferencePointY.toInt()
                right = bearingReferencePointX.toInt() + childView.measuredWidth
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight
            }
            TOP -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth / 2
                top = bearingReferencePointY.toInt()
                right = bearingReferencePointX.toInt() + childView.measuredWidth / 2
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight
            }
            TOP_RIGHT -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth
                top = bearingReferencePointY.toInt()
                right = bearingReferencePointX.toInt()
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight
            }
            CENTER_LEFT -> {
                left = bearingReferencePointX.toInt()
                top = bearingReferencePointY.toInt() - childView.measuredHeight / 2
                right = bearingReferencePointX.toInt() + childView.measuredWidth
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight / 2
            }
            CENTER -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth / 2
                top = bearingReferencePointY.toInt() - childView.measuredHeight / 2
                right = bearingReferencePointX.toInt() + childView.measuredWidth / 2
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight / 2
            }
            CENTER_RIGHT -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth
                top = bearingReferencePointY.toInt() - childView.measuredHeight / 2
                right = bearingReferencePointX.toInt()
                bottom = bearingReferencePointY.toInt() + childView.measuredHeight / 2
            }
            BOTTOM_LEFT -> {
                left = bearingReferencePointX.toInt()
                top = bearingReferencePointY.toInt() - childView.measuredHeight
                right = bearingReferencePointX.toInt() + childView.measuredWidth
                bottom = bearingReferencePointY.toInt()
            }
            BOTTOM -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth / 2
                top = bearingReferencePointY.toInt() - childView.measuredHeight
                right = bearingReferencePointX.toInt() + childView.measuredWidth / 2
                bottom = bearingReferencePointY.toInt()
            }
            BOTTOM_RIGHT -> {
                left = bearingReferencePointX.toInt() - childView.measuredWidth
                top = bearingReferencePointY.toInt() - childView.measuredHeight
                right = bearingReferencePointX.toInt()
                bottom = bearingReferencePointY.toInt()
            }
        }

        if (adjustToParentBounds) {
            var adjustedLeft = left
            var adjustedTop = top
            var adjustedRight = right
            var adjustedBottom = bottom

            if (adjustedRight > width) {
                val diff = adjustedRight - width

                adjustedLeft = max(0, adjustedLeft - diff)
                adjustedRight = min(width, adjustedRight - diff)
            }

            if (adjustedBottom > height) {
                val diff = adjustedBottom - height

                adjustedTop = max(0, adjustedTop - diff)
                adjustedBottom = min(height, adjustedBottom - diff)
            }

            outRect.set(adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
        } else {
            outRect.set(left, top, right, bottom)
        }
    }

    private fun calculateTotalDistance(
        childView: View,
        bearingRelativeToAnchor: Float,
        bearingReference: BearingReference,
        rad: Double,
        anchorViewRect: Rect,
        distanceFromAnchorPx: Int,
        allowAnchorTooltipOverlap: Boolean,
    ): Int {
        if (allowAnchorTooltipOverlap) {
            return distanceFromAnchorPx
        }

        val anchorInnerDistance =
            calculateViewInnerDistance(
                bearingRelativeToAnchor,
                CENTER,
                rad,
                anchorViewRect.height(),
                anchorViewRect.width()
            )
        val childInnerDistance =
            calculateViewInnerDistance(
                bearingRelativeToAnchor,
                bearingReference,
                rad,
                childView.measuredHeight,
                childView.measuredWidth
            )
        val totalDistance = anchorInnerDistance + distanceFromAnchorPx + childInnerDistance

        return totalDistance.toInt()
    }

    private fun calculateViewInnerDistance(
        bearingRelativeToAnchor: Float,
        bearingReference: BearingReference,
        rad: Double,
        height: Int,
        width: Int,
    ): Double {
        val adjustedWidth: Int
        val adjustedHeight: Int

        if (bearingReference == CENTER) {
            adjustedWidth = width / 2
            adjustedHeight = height / 2
        } else {
            when (bearingRelativeToAnchor) {
                in (0F..90F) -> {
                    when (bearingReference) {
                        TOP -> {
                            adjustedWidth = width / 2
                            adjustedHeight = height
                        }
                        TOP_RIGHT -> {
                            adjustedWidth = width
                            adjustedHeight = height
                        }
                        CENTER_RIGHT -> {
                            adjustedWidth = width
                            adjustedHeight = height / 2
                        }
                        else -> {
                            return 0.0
                        }
                    }
                }
                in (90F..180F) -> {
                    when (bearingReference) {
                        BOTTOM -> {
                            adjustedWidth = width / 2
                            adjustedHeight = height
                        }
                        BOTTOM_RIGHT -> {
                            adjustedWidth = width
                            adjustedHeight = height
                        }
                        CENTER_RIGHT -> {
                            adjustedWidth = width
                            adjustedHeight = height / 2
                        }
                        else -> {
                            return 0.0
                        }
                    }
                }
                in (180F..270F) -> {
                    when (bearingReference) {
                        BOTTOM -> {
                            adjustedWidth = width / 2
                            adjustedHeight = height
                        }
                        BOTTOM_LEFT -> {
                            adjustedWidth = width
                            adjustedHeight = height
                        }
                        CENTER_LEFT -> {
                            adjustedWidth = width
                            adjustedHeight = height / 2
                        }
                        else -> {
                            return 0.0
                        }
                    }
                }
                else -> {
                    when (bearingReference) {
                        TOP -> {
                            adjustedWidth = width / 2
                            adjustedHeight = height
                        }
                        TOP_LEFT -> {
                            adjustedWidth = width
                            adjustedHeight = height
                        }
                        CENTER_LEFT -> {
                            adjustedWidth = width
                            adjustedHeight = height / 2
                        }
                        else -> {
                            return 0.0
                        }
                    }
                }
            }
        }

        val thresholdRad = atan(adjustedHeight / adjustedWidth.toDouble())

        return if (rad > thresholdRad) {
            adjustedHeight / sin(rad)
        } else {
            adjustedWidth / cos(rad)
        }
    }

    class LayoutParams private constructor(
        width: Int,
        height: Int,
        internal val anchor: Anchor,
        internal val hasFixedPosition: Boolean,
        internal val adjustToParentBounds: Boolean
    ) : ViewGroup.LayoutParams(width, height) {

        companion object {

            const val BEARING_TOP_DEGREES = 0F
            const val BEARING_RIGHT_DEGREES = 90F
            const val BEARING_BOTTOM_DEGREES = 180F
            const val BEARING_LEFT_DEGREES = 270F

            private const val DEFAULT_THRESHOLD_BEARING = 75F

            /**
             * Position a tooltip relative to its anchor given a bearing between the anchor's center (A)
             * and one of the nine tooltip's points of choice (bearing reference).
             *
             *                                            ANCHOR
             *                                          _________
             *                                         |         |
             *                                         |  A *    |
             *                                         |_________|
             *
             *                TOOLTIP
             *      TL           T           TR
             *       *___________*___________*
             *       |                       |
             *    CL *         C *           * CR
             *       |                       |
             *       *___________*___________*
             *      BL           B            BR
             *
             *
             * @param width Width of the tooltip view. May be ViewGroup.LayoutParams.WRAP_CONTENT,
             *  ViewGroup.LayoutParams.MATCH_PARENT or exact size.
             * @param height Height of the tooltip view. May be ViewGroup.LayoutParams.WRAP_CONTENT,
             *  ViewGroup.LayoutParams.MATCH_PARENT or exact size.
             * @param anchor View relative to which the tooltip will be positioned. Most likely this is
             *  the view you want highlighted.
             * @param distanceFromAnchorPx Distance that is interpreted as per allowAnchorTooltipOverlap.
             * @param bearing Given anchor's center as a coordinates center, defines the
             *  angle between Y-axis (directed upward) and a line connecting anchor's center and a
             *  tooltip's point defined by bearingReference.
             * @param bearingReference Defines a point on a tooltip's frame that serves as a reference
             *  for bearing.
             * @param allowAnchorTooltipOverlap If true, distanceFromAnchorPx is interpreted as distance
             *  between anchor's center and a tooltip's point defined by bearingReference;
             *  if false, as distance between anchor's and tooltip's frames.
             * @param hasFixedPosition Defines whether hint's position must be fixed. If true, hint will
             *  be drawn only once. If false, hint will be redrawn each time the anchor is redrawn. This
             *  behavior can be useful if the anchor is not static on the screen.
             * @param adjustToParentBounds If, after positioning, tooltip frame ends up partially or
             *  completely outside of overlay bounds, it will be pushed to the visible area unless this
             *  flag is false.
             *
             * @return Layout params
             */
            fun createRelativeToAnchor(
                width: Int,
                height: Int,
                anchor: View,
                @Px distanceFromAnchorPx: Int,
                @FloatRange(from = 0.0, to = 360.0) bearing: Float,
                bearingReference: BearingReference,
                allowAnchorTooltipOverlap: Boolean = false,
                hasFixedPosition: Boolean = true,
                adjustToParentBounds: Boolean = true
            ): LayoutParams {
                return LayoutParams(
                    width = width,
                    height = height,
                    anchor = Anchor(
                        anchorViewRef = WeakReference(anchor),
                        distanceFromAnchorPx = distanceFromAnchorPx,
                        allowAnchorTooltipOverlap = allowAnchorTooltipOverlap,
                        bearingRelativeToAnchor = bearing,
                        bearingReference = bearingReference
                    ),
                    hasFixedPosition = hasFixedPosition,
                    adjustToParentBounds = adjustToParentBounds
                )
            }

            @FloatRange(from = 0.0, to = 360.0)
            fun suggestBearing(
                spotlightController: SpotlightController,
                anchor: View,
                thresholdBearing: Float? = null
            ): Float {
                val overlayParent = spotlightController.parent
                val coords = anchor.offsetRectToAncestorCoords(overlayParent)

                val normalizedPosition = coords.centerX() / overlayParent.width.toFloat()
                val showTooltipBelow = coords.centerY() < (overlayParent.height / 2)

                val actualThresholdBearing = thresholdBearing ?: DEFAULT_THRESHOLD_BEARING
                val bearingDegreesUnadjusted: Float = if (normalizedPosition in (0F..0.5F)) {
                    actualThresholdBearing - 2 * normalizedPosition * actualThresholdBearing
                } else {
                    -2 * (normalizedPosition - 0.5F) * actualThresholdBearing
                }

                return if (showTooltipBelow) {
                    (180F - bearingDegreesUnadjusted)
                } else {
                    bearingDegreesUnadjusted
                }.asBearing()
            }

            fun suggestBearingReference(
                @FloatRange(from = 0.0, to = 360.0) bearing: Float
            ): BearingReference {
                return when (bearing) {
                    BEARING_LEFT_DEGREES -> {
                        CENTER_RIGHT
                    }
                    BEARING_RIGHT_DEGREES -> {
                        CENTER_LEFT
                    }
                    BEARING_TOP_DEGREES -> {
                        BOTTOM
                    }
                    BEARING_BOTTOM_DEGREES -> {
                        TOP
                    }
                    in (0F..90F) -> {
                        BOTTOM_LEFT
                    }
                    in (90F..180F) -> {
                        TOP_LEFT
                    }
                    in (180F..270F) -> {
                        TOP_RIGHT
                    }
                    in (270F..360F) -> {
                        BOTTOM_RIGHT
                    }
                    else -> {
                        throw IllegalArgumentException(
                            "Parameter expected to be in a range [0.0..360.0] " +
                                    "but was $bearing"
                        )
                    }
                }
            }

            private fun Float.asBearing(): Float {
                return if (this >= 0) this else 360 + this
            }

        }

        internal class Anchor(
            val anchorViewRef: WeakReference<View>,
            @Px val distanceFromAnchorPx: Int,
            val allowAnchorTooltipOverlap: Boolean,
            @FloatRange(from = 0.0, to = 360.0) val bearingRelativeToAnchor: Float,
            val bearingReference: BearingReference
        )

    }

}
