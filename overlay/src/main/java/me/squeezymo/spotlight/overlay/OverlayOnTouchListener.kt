package me.squeezymo.spotlight.overlay

import android.view.MotionEvent
import android.view.View

interface OverlayOnTouchListener {

    /**
     * Callback to be invoked when overlay receives motion event.
     *
     * @param overlayView Overlay view within whose bounds the motion event was performed.
     * @param event The motion event.
     * @param accentedView The accented view over which the motion event was performed. If null, there
     * is no accented view under the touch event area.
     *
     * @return true if the event was consumed by overlay and must not be passed down to accented view;
     * false otherwise
     */
    fun onTouchEvent(overlayView: View, event: MotionEvent, accentedView: View?): Boolean

    /**
     * Creates click listener to be invoked when overlay receives click event.
     */
    fun createClickListener(): View.OnClickListener?

}
