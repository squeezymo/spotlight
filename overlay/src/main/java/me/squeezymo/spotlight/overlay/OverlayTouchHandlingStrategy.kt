package me.squeezymo.spotlight.overlay

import android.view.View
import me.squeezymo.spotlight.overlay.internal.touch.AllInterceptingOnTouchListener
import me.squeezymo.spotlight.overlay.internal.touch.NotAccentedInterceptingOnTouchListener

/**
 * Class that specifies touch handling strategy. Touch events will be passed to the provided
 * listener in accordance with the respective strategy.
 */
sealed class OverlayTouchHandlingStrategy(
    internal val listener: OverlayOnTouchListener
) {

    /**
     * listener will receive clicks only from areas outside of accented views. Events performed over
     * accented views will be passed down to them.
     */
    class NonAccentedClicksListener(
        clickListener: View.OnClickListener
    ) : OverlayTouchHandlingStrategy(NotAccentedInterceptingOnTouchListener(clickListener))

    /**
     * listener will receive clicks from anywhere within overlay bounds. Touch events will NOT be
     * passed to accented views.
     */
    class AllClicksListener(
        clickListener: View.OnClickListener
    ) : OverlayTouchHandlingStrategy(AllInterceptingOnTouchListener(clickListener))

    /**
     * Custom handling for touch events.
     */
    class CustomHandler(
        onTouchListener: OverlayOnTouchListener
    ) : OverlayTouchHandlingStrategy(onTouchListener)

}
