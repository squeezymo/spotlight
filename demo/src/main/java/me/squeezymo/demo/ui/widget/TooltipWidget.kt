package me.squeezymo.demo.ui.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import me.squeezymo.demo.R
import me.squeezymo.demo.ui.util.dp
import me.squeezymo.spotlight.tooltiparrow.ArrowView

class TooltipWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {

        fun create(
            context: Context,
            text: String,
            showArrow: Boolean,
            @FloatRange(from = 0.0, to = 360.0) tooltipBearing: Float,
            onAcknowledged: ((TooltipWidget) -> Unit)? = null
        ): TooltipWidget {
            return TooltipWidget(context).apply {
                setText(text)
                adjustArrowView(showArrow, tooltipBearing)
                setOnAcknowledgedListener(onAcknowledged)
            }
        }

    }

    init {
        LayoutInflater
            .from(context)
            .inflate(R.layout.v_tooltip, this, true)

        orientation = VERTICAL
    }

    private var shouldShowArrow: Boolean = false

    @FloatRange(from = 0.0, to = 360.0)
    private var tooltipBearing: Float = 0F

    private var pointerAv: ArrowView? = null
    private val helperTv: TextView = findViewById(R.id.helper_tv)
    private val acknowledgeBtn: Button = findViewById(R.id.acknowledge_btn)

    init {
        if (isInEditMode) {
            adjustArrowView(true, 45F)
        }
    }

    fun setText(text: String) {
        helperTv.text = text
    }

    fun setShowArrow(
        shouldShowArrow: Boolean
    ) {
        adjustArrowView(shouldShowArrow, tooltipBearing)
    }

    fun setTooltipBearing(
        @FloatRange(from = 0.0, to = 360.0) bearing: Float
    ) {
        adjustArrowView(shouldShowArrow, bearing)
    }

    private fun adjustArrowView(
        shouldShowArrow: Boolean,
        @FloatRange(from = 0.0, to = 360.0) tooltipBearing: Float
    ) {
        this.shouldShowArrow = shouldShowArrow
        this.tooltipBearing = tooltipBearing

        pointerAv?.let(::removeView)

        if (!shouldShowArrow) {
            pointerAv = null
            return
        }

        val arrowView = pointerAv
            ?: ArrowView(context)
                .apply(::setupArrowView)
                .also {
                    pointerAv = it
                }

        val arrowBearing = (tooltipBearing - 180).asBearing()

        when (arrowBearing) {
            in (0F..90F), in (270F..360F) -> {
                addView(arrowView, 0)
            }
            else -> {
                addView(arrowView, childCount)
            }
        }

        arrowView.setBearing(arrowBearing)
        arrowView.updateLayoutParams<LayoutParams> {
            gravity = when (arrowBearing) {
                in (10F..170F) -> Gravity.END
                in (190F..350F) -> Gravity.START
                else -> Gravity.CENTER_HORIZONTAL
            }

            when (arrowBearing) {
                in (0F..90F), in (270F..360F) -> {
                    topMargin = 0
                    bottomMargin = dp(8)
                }
                else -> {
                    topMargin = dp(8)
                    bottomMargin = 0
                }
            }
        }
    }

    private fun setupArrowView(av: ArrowView) {
        with(av) {
            enableArrowhead(true)
            setArrowheadHeight(dp(12).toFloat())
            setArrowheadMargin(dp(8).toFloat())
            setArrowheadWidth(dp(8).toFloat())
            setColor(ContextCompat.getColor(context, R.color.white))
            setLength(dp(48).toFloat())
            setWidth(dp(2).toFloat())
            setWiggleFactor(0.9F)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun setOnAcknowledgedListener(listener: ((TooltipWidget) -> Unit)?) {
        if (listener == null) {
            acknowledgeBtn.visibility = View.GONE
        } else {
            acknowledgeBtn.visibility = View.VISIBLE
            acknowledgeBtn.setOnClickListener {
                listener(this)
            }
        }
    }

    fun createInAnimator(): Animator {
        return AnimatorSet().apply {
            doOnStart {
                helperTv.alpha = 0F
                acknowledgeBtn.alpha = 0F
            }

            playTogether(
                listOfNotNull(
                    pointerAv?.createAnimator().apply {
                        duration = 250L
                    },
                    ValueAnimator
                        .ofFloat(0F, 1F)
                        .apply {
                            addUpdateListener { valueAnimator ->
                                val alpha = valueAnimator.animatedValue as Float

                                helperTv.alpha = alpha
                                acknowledgeBtn.alpha = alpha
                            }

                            duration = 300L
                            startDelay = 200L
                        }
                )
            )
        }
    }

    private fun Float.asBearing(): Float {
        return if (this >= 0) this else 360 + this
    }

}
