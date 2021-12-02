package me.squeezymo.spotlight.overlay.internal.touch

import android.view.MotionEvent
import android.view.View
import me.squeezymo.spotlight.overlay.OverlayOnTouchListener

internal class NotAccentedInterceptingOnTouchListener(
    private val clickListener: View.OnClickListener
) : OverlayOnTouchListener {

    override fun onTouchEvent(overlayView: View, event: MotionEvent, accentedView: View?): Boolean {
        return accentedView == null
    }

    override fun createClickListener() = clickListener

}
