package me.squeezymo.spotlight.overlay.internal.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import me.squeezymo.spotlight.overlay.AccentParams
import me.squeezymo.spotlight.overlay.OverlayOnTouchListener
import me.squeezymo.spotlight.overlay.ext.doWithTranslation
import me.squeezymo.spotlight.overlay.ext.offsetRectToAncestorCoords
import me.squeezymo.spotlight.overlay.internal.touch.AllInterceptingOnTouchListener
import java.util.*
import kotlin.math.abs
import kotlin.math.max

internal class DimmingOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {

        private const val CLICK_THRESHOLD_MILLIS = 200L
        private const val CLICK_THRESHOLD_PX = 20

    }

    private val accents: MutableMap<View, AccentParams> = WeakHashMap()

    private lateinit var bitmap: Bitmap
    private lateinit var innerCanvas: Canvas
    private var dimmingColor: Int = 0
    private var overlayOnTouchListener: OverlayOnTouchListener = AllInterceptingOnTouchListener {
        Log.w(DimmingOverlayView::class.java.simpleName, "Click event detected but not handled")
    }
    private val viewDrawingRect = Rect()
    private val whitePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }

    private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
        invalidate()
        true
    }

    private val actionDownCoords = PointF()
    private var actionDownMillis: Long = 0
    private var isClickExpected = false

    fun setAccents(
        accents: Map<View, AccentParams>,
        invalidateImmediately: Boolean
    ) {
        clear()
        this.accents.putAll(accents)

        for ((anchor, params) in accents) {
            if (params.invalidateAccentOnRedraws) {
                val treeObserver = anchor.viewTreeObserver
                if (treeObserver?.isAlive == true) {
                    treeObserver.addOnPreDrawListener(onPreDrawListener)
                }
            }
        }

        if (invalidateImmediately) {
            invalidate()
        }
    }

    fun clear() {
        accents.keys.forEach { anchor ->
            val treeObserver = anchor.viewTreeObserver
            if (treeObserver?.isAlive == true) {
                treeObserver.removeOnPreDrawListener(onPreDrawListener)
            }
        }
        accents.clear()
    }

    fun setOverlayOnTouchListener(overlayOnTouchListener: OverlayOnTouchListener) {
        this.overlayOnTouchListener = overlayOnTouchListener
        setOnClickListener(overlayOnTouchListener.createClickListener())
    }

    fun setDimmingColor(@ColorInt color: Int) {
        this.dimmingColor = color
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                actionDownMillis = System.currentTimeMillis()
                isClickExpected = true
                actionDownCoords.set(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - actionDownCoords.x) > CLICK_THRESHOLD_PX || abs(event.y - actionDownCoords.y) > CLICK_THRESHOLD_PX) {
                    isClickExpected = false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isClickExpected && System.currentTimeMillis() - actionDownMillis < CLICK_THRESHOLD_MILLIS) {
                    performClick()
                }
            }
        }

        for ((accentedView, _) in accents) {
            if (accentedView.isVisible) {
                accentedView.offsetRectToAncestorCoords(parent as ViewGroup, viewDrawingRect)

                val left = viewDrawingRect.left.toFloat() + accentedView.translationX
                val top = viewDrawingRect.top.toFloat() + accentedView.translationY

                val isEventOnAccentedView =
                    event.x in (left..left + accentedView.width) && event.y in (top..top + accentedView.height)

                if (isEventOnAccentedView) {
                    return overlayOnTouchListener.onTouchEvent(this, event, accentedView)
                }
            }
        }

        return overlayOnTouchListener.onTouchEvent(this, event, null)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        innerCanvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || (width == 0 && height == 0)) {
            return
        }

        bitmap.eraseColor(Color.TRANSPARENT)

        for ((view, params) in accents) {
            if (view.isVisible) {
                drawTransparentAreaOverView(view, params)
            }
        }

        innerCanvas.drawColor(dimmingColor, PorterDuff.Mode.SRC_OUT)

        canvas.drawBitmap(bitmap, 0F, 0F, null)
    }

    private fun drawTransparentAreaOverView(view: View, params: AccentParams) {
        view.offsetRectToAncestorCoords(parent as ViewGroup, viewDrawingRect)

        innerCanvas.doWithTranslation(
            dx = viewDrawingRect.left.toFloat() + view.translationX,
            dy = viewDrawingRect.top.toFloat() + view.translationY
        ) {
            innerCanvas.scale(
                params.accentShape.scale * view.scaleX,
                params.accentShape.scale * view.scaleY,
                view.pivotX,
                view.pivotY
            )

            when (val shape = params.accentShape) {
                is AccentParams.Shape.ExactViewShape -> {
                    view.draw(innerCanvas)
                }
                is AccentParams.Shape.Circle -> {
                    innerCanvas.drawCircle(
                        view.width / 2F,
                        view.height / 2F,
                        params.accentShape.scale * max(view.width, view.height) / 2F,
                        whitePaint
                    )
                }
                is AccentParams.Shape.Oval -> {
                    innerCanvas.drawOval(
                        0F,
                        0F,
                        view.width.toFloat(),
                        view.height.toFloat(),
                        whitePaint
                    )
                }
                is AccentParams.Shape.Rectangle -> {
                    innerCanvas.drawRoundRect(
                        0F,
                        0F,
                        view.width.toFloat(),
                        view.height.toFloat(),
                        shape.roundPx.toFloat(),
                        shape.roundPx.toFloat(),
                        whitePaint
                    )
                }
            }
        }
    }

}
