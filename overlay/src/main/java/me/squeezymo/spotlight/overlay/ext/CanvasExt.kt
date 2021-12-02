@file:Suppress("NOTHING_TO_INLINE")

package me.squeezymo.spotlight.overlay.ext

import android.graphics.Canvas

internal inline fun Canvas.doWithTranslation(
    dx: Float,
    dy: Float,
    block: () -> Unit
) {
    save()
    translate(dx, dy)
    block()
    restore()
}
