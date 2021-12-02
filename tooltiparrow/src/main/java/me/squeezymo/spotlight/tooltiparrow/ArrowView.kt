package me.squeezymo.spotlight.tooltiparrow

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.animation.doOnStart
import me.squeezymo.spotlight.tooltiparrow.ext.use
import kotlin.math.*

class ArrowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {

        private const val PROP_SEGMENT_FACTOR = "segment_factor"

    }

    @FloatRange(from = 0.0, to = 360.0)
    private var bearingDegrees: Float = 0F

    @Px
    private var length: Float = 0F

    private var wiggleFactor: Float = 0F

    @FloatRange(from = 0.0, to = 1.0)
    private var shaftDrawingSegmentFactor: Float = 1F

    @FloatRange(from = 0.0, to = 1.0)
    private var arrowheadDrawingSegmentFactor: Float = 1F

    private var arrowheadEnabled: Boolean = true

    @Px
    private var arrowheadMargin: Float = 0F

    @Px
    private var arrowheadWidth: Float = 0F

    @Px
    private var arrowheadHeight: Float = 0F

    private val mainPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val shaftPath = Path()
    private val shaftPathMeasure = PathMeasure(shaftPath, false)
    private val shaftPartialPath = Path()
    private val shaftRect = RectF()

    private val arrowheadPath = Path()
    private val arrowheadPathMeasure = PathMeasure(shaftPath, false)
    private val arrowheadPartialPath = Path()
    private val arrowheadRect = RectF()

    private val controlPoint = PointF()
    private val basePoint = PointF()
    private val tipPoint = PointF()
    private val sidePoint1 = PointF()
    private val sidePoint2 = PointF()

    private val arrowheadCanBeDrawn: Boolean
        get() = arrowheadEnabled && arrowheadHeight != 0F && arrowheadWidth != 0F

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ArrowView,
            0,
            0
        ).use {
            setBearing(getFloat(R.styleable.ArrowView_av_bearingDegrees, bearingDegrees))
            setLength(getDimension(R.styleable.ArrowView_av_length, length))
            setWidth(getDimension(R.styleable.ArrowView_av_width, mainPaint.strokeWidth))
            setColor(getColor(R.styleable.ArrowView_av_color, mainPaint.color))
            setWiggleFactor(getFloat(R.styleable.ArrowView_av_wiggleFactor, wiggleFactor))
            enableArrowhead(getBoolean(R.styleable.ArrowView_av_arrowheadEnabled, arrowheadEnabled))
            setArrowheadMargin(
                getDimension(
                    R.styleable.ArrowView_av_arrowheadMargin,
                    arrowheadMargin
                )
            )
            setArrowheadWidth(getDimension(R.styleable.ArrowView_av_arrowheadWidth, arrowheadWidth))
            setArrowheadHeight(
                getDimension(
                    R.styleable.ArrowView_av_arrowheadHeight,
                    arrowheadHeight
                )
            )
        }
    }

    fun setBearing(@FloatRange(from = 0.0, to = 360.0) bearing: Float) {
        check(bearing in (0F..360F)) {
            "Parameter expected to be in a range [0.0..360.0] but was $bearing"
        }

        this.bearingDegrees = bearing
        requestLayout()
    }

    fun setLength(@Px length: Float) {
        this.length = length
        requestLayout()
    }

    fun setWidth(@Px width: Float) {
        mainPaint.strokeWidth = width
        requestLayout()
    }

    fun setColor(@ColorInt color: Int) {
        mainPaint.color = color
        invalidate()
    }

    fun setWiggleFactor(@FloatRange(from = 0.0, to = 1.0) wiggleFactor: Float) {
        this.wiggleFactor = wiggleFactor
        invalidate()
    }

    fun enableArrowhead(arrowheadEnabled: Boolean) {
        this.arrowheadEnabled = arrowheadEnabled
        requestLayout()
    }

    fun setArrowheadMargin(@Px arrowheadMargin: Float) {
        this.arrowheadMargin = arrowheadMargin
        requestLayout()
    }

    fun setArrowheadWidth(@Px arrowheadWidth: Float) {
        this.arrowheadWidth = arrowheadWidth
        requestLayout()
    }

    fun setArrowheadHeight(@Px arrowheadHeight: Float) {
        this.arrowheadHeight = arrowheadHeight
        requestLayout()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        check(
            params == null ||
                    (params.width == ViewGroup.LayoutParams.WRAP_CONTENT && params.height == ViewGroup.LayoutParams.WRAP_CONTENT)
        ) {
            "${ArrowView::class.java.canonicalName} supports only wrap_content for its dimensions. " +
                    "Please use av_bearingDegrees and av_length and/or respective setters to control view's size and shape"
        }

        super.setLayoutParams(params)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val alpha = when (bearingDegrees) {
            in (0F..90F) -> bearingDegrees
            in (90F..180F) -> 180 - bearingDegrees
            in (180F..270F) -> bearingDegrees - 180
            else -> 360 - bearingDegrees
        }

        val rad = Math.toRadians(alpha.toDouble())
        val sinRad = sin(rad)
        val cosRad = cos(rad)
        val shaftWidth = length * sinRad
        val shaftHeight = length * cosRad

        if (arrowheadCanBeDrawn) {
            prepareArrowheadPath(sinRad, cosRad, shaftWidth.toFloat(), shaftHeight.toFloat())
        } else {
            controlPoint.set(0F, 0F)
        }

        prepareShaftPath(
            sinRad,
            cosRad,
            shaftWidth.toFloat(),
            shaftHeight.toFloat(),
            dx = controlPoint.x,
            dy = controlPoint.y
        )

        shaftPath.computeBounds(shaftRect, true)
        arrowheadPath.computeBounds(arrowheadRect, true)

        val width =
            max(shaftRect.right, arrowheadRect.right) - min(shaftRect.left, arrowheadRect.left)
        val height =
            max(shaftRect.bottom, arrowheadRect.bottom) - min(shaftRect.top, arrowheadRect.top)

        setMeasuredDimension(
            width.toInt() + 2 * mainPaint.strokeWidth.toInt(),
            height.toInt() + 2 * mainPaint.strokeWidth.toInt()
        )
    }

    private fun prepareShaftPath(
        sinRad: Double,
        cosRad: Double,
        width: Float,
        height: Float,
        dx: Float,
        dy: Float
    ) {
        shaftPath.reset()

        val shaftWidth: Float
        val shaftHeight: Float

        if (arrowheadCanBeDrawn && arrowheadMargin != 0F) {
            val fullDiag = sqrt(width * width + height * height)
            val shaftDiag = fullDiag - arrowheadMargin

            shaftWidth = shaftDiag * sinRad.toFloat()
            shaftHeight = shaftDiag * cosRad.toFloat()
        } else {
            shaftWidth = width
            shaftHeight = height
        }

        val controlPoint1X: Float
        val controlPoint1Y: Float
        val controlPoint2X: Float
        val controlPoint2Y: Float
        val endPointX: Float
        val endPointY: Float

        when (bearingDegrees) {
            in (0F..90F) -> {
                shaftPath.moveTo(dx, height + dy)

                val heightDelta = height - shaftHeight

                calcBottomLeftControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint1X = controlPoint.x + dx
                controlPoint1Y = controlPoint.y + heightDelta + dy

                calcTopRightControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint2X = controlPoint.x + dx
                controlPoint2Y = controlPoint.y + heightDelta + dy

                endPointX = shaftWidth + dx
                endPointY = heightDelta + dy
            }
            in (90F..180F) -> {
                shaftPath.moveTo(dx, dy)

                calcTopLeftControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint1X = controlPoint.x + dx
                controlPoint1Y = controlPoint.y + dy

                calcBottomRightControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint2X = controlPoint.x + dx
                controlPoint2Y = controlPoint.y + dy

                endPointX = shaftWidth + dx
                endPointY = shaftHeight + dy
            }
            in (180F..270F) -> {
                shaftPath.moveTo(width + dx, dy)

                calcTopRightControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint1X = controlPoint.x + dx
                controlPoint1Y = controlPoint.y + dy

                calcBottomLeftControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint2X = controlPoint.x + dx
                controlPoint2Y = controlPoint.y + dy

                endPointX = width - shaftWidth + dx
                endPointY = shaftHeight + dy
            }
            else -> {
                shaftPath.moveTo(width + dx, height + dy)

                calcBottomRightControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint1X = controlPoint.x + dx
                controlPoint1Y = controlPoint.y + dy

                calcTopLeftControlPoint(shaftWidth, shaftHeight, controlPoint)
                controlPoint2X = controlPoint.x + dx
                controlPoint2Y = controlPoint.y + dy

                endPointX = width - shaftWidth + dx
                endPointY = height - shaftHeight + dy
            }
        }

        shaftPath.cubicTo(
            controlPoint1X,
            controlPoint1Y,
            controlPoint2X,
            controlPoint2Y,
            endPointX,
            endPointY
        )

        shaftPathMeasure.setPath(shaftPath, false)
    }

    private fun prepareArrowheadPath(sinRad: Double, cosRad: Double, width: Float, height: Float) {
        arrowheadPath.reset()

        val fullDiag = sqrt(width * width + height * height)
        val fullShaftDiag = fullDiag - arrowheadHeight

        val fullShaftWidth = fullShaftDiag * sinRad.toFloat()
        val fullShaftHeight = fullShaftDiag * cosRad.toFloat()

        when (bearingDegrees) {
            in (0F..90F) -> {
                basePoint.set(fullShaftWidth, height - fullShaftHeight)
                tipPoint.set(width, 0F)
            }
            in (90F..180F) -> {
                basePoint.set(fullShaftWidth, fullShaftHeight)
                tipPoint.set(width, height)
            }
            in (180F..270F) -> {
                basePoint.set(width - fullShaftWidth, fullShaftHeight)
                tipPoint.set(0F, height)
            }
            else -> {
                basePoint.set(width - fullShaftWidth, height - fullShaftHeight)
                tipPoint.set(0F, 0F)
            }
        }

        val arrowheadHalfWidth = arrowheadWidth / 2

        calcArrowheadSidePoints(basePoint, tipPoint, arrowheadHalfWidth, sidePoint1, sidePoint2)

        // Track drawing outside the bounds
        controlPoint.set(
            -(min(sidePoint1.x, sidePoint2.x).coerceAtMost(0F)) + mainPaint.strokeWidth,
            -(min(sidePoint1.y, sidePoint2.y).coerceAtMost(0F)) + mainPaint.strokeWidth
        )

        basePoint.set(basePoint.x + controlPoint.x, basePoint.y + controlPoint.y)
        tipPoint.set(tipPoint.x + controlPoint.x, tipPoint.y + controlPoint.y)
        sidePoint1.set(sidePoint1.x + controlPoint.x, sidePoint1.y + controlPoint.y)
        sidePoint2.set(sidePoint2.x + controlPoint.x, sidePoint2.y + controlPoint.y)

        arrowheadPath.moveTo(sidePoint1.x, sidePoint1.y)
        arrowheadPath.lineTo(tipPoint.x, tipPoint.y)
        arrowheadPath.lineTo(sidePoint2.x, sidePoint2.y)

        arrowheadPathMeasure.setPath(arrowheadPath, false)
    }

    private fun calcTopLeftControlPoint(width: Float, height: Float, outPoint: PointF) {
        outPoint.x = interpolateLinear(width / 4, width / 2, wiggleFactor)
        outPoint.y = height / 4
    }

    private fun calcTopRightControlPoint(width: Float, height: Float, outPoint: PointF) {
        outPoint.x = interpolateLinear(3 * width / 4, width / 2, wiggleFactor)
        outPoint.y = height / 4
    }

    private fun calcBottomLeftControlPoint(width: Float, height: Float, outPoint: PointF) {
        outPoint.x = interpolateLinear(width / 4, width / 2, wiggleFactor)
        outPoint.y = 3 * height / 4
    }

    private fun calcBottomRightControlPoint(width: Float, height: Float, outPoint: PointF) {
        outPoint.x = interpolateLinear(3 * width / 4, width / 2, wiggleFactor)
        outPoint.y = 3 * height / 4
    }

    /*
     * Given the coordinates of points A and C and the length of leg a,
     * find the coordinates of points B1 and B2.
     *
     *            A
     *           /|\
     *          / | \
     *         /  |  \
     *      c /   |   \ c
     *       /    |    \
     *      /     |     \
     *     /______|______\
     *  B1(?)  a  C  a  B2(?)
     */
    private fun calcArrowheadSidePoints(
        pointC: PointF,
        pointA: PointF,
        a: Float,
        outPointB1: PointF,
        outPointB2: PointF
    ) {
        controlPoint.set(pointA.x - pointC.x, pointA.y - pointC.y)

        // Normalization
        val len = sqrt(controlPoint.x * controlPoint.x + controlPoint.y * controlPoint.y)
        controlPoint.set(controlPoint.x / len, controlPoint.y / len)

        // Counterclockwise rotation
        outPointB1.set(controlPoint.y, -controlPoint.x)

        // Clockwise rotation
        outPointB2.set(-controlPoint.y, controlPoint.x)

        outPointB1.set(a * outPointB1.x + pointC.x, a * outPointB1.y + pointC.y)
        outPointB2.set(a * outPointB2.x + pointC.x, a * outPointB2.y + pointC.y)
    }

    private fun interpolateLinear(
        from: Float,
        to: Float,
        r: Float
    ): Float {
        return from + r * (to - from)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            return
        }

        drawShaft(canvas)
        if (arrowheadCanBeDrawn) {
            drawArrowhead(canvas)
        }
    }

    private fun drawShaft(canvas: Canvas) {
        val pathLength = shaftPathMeasure.length

        shaftPathMeasure.getSegment(
            0F,
            shaftDrawingSegmentFactor * pathLength,
            shaftPartialPath,
            true
        )

        canvas.drawPath(shaftPartialPath, mainPaint)
    }

    private fun drawArrowhead(canvas: Canvas) {
        val pathLength = arrowheadPathMeasure.length

        arrowheadPathMeasure.getSegment(
            0F,
            arrowheadDrawingSegmentFactor * pathLength,
            arrowheadPartialPath,
            true
        )

        canvas.drawPath(arrowheadPartialPath, mainPaint)
    }

    fun createAnimator(): Animator {
        return AnimatorSet().apply {
            doOnStart {
                shaftDrawingSegmentFactor = 0F
                arrowheadDrawingSegmentFactor = 0F
            }

            playSequentially(
                createShaftAnimator().apply {
                    duration = 250
                },
                createArrowheadAnimator().apply {
                    duration = 150
                }
            )
        }
    }

    private fun createShaftAnimator(): ValueAnimator {
        return ValueAnimator().apply {
            setValues(PropertyValuesHolder.ofFloat(PROP_SEGMENT_FACTOR, 0F, 1F))

            addUpdateListener {
                shaftDrawingSegmentFactor = it.getAnimatedValue(PROP_SEGMENT_FACTOR) as Float
                invalidate()
            }

            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun createArrowheadAnimator(): ValueAnimator {
        return ValueAnimator().apply {
            setValues(PropertyValuesHolder.ofFloat(PROP_SEGMENT_FACTOR, 0F, 1F))

            addUpdateListener {
                arrowheadDrawingSegmentFactor = it.getAnimatedValue(PROP_SEGMENT_FACTOR) as Float
                invalidate()
            }

            interpolator = LinearInterpolator()
        }
    }

}
