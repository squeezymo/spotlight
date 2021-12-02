@file:Suppress("NOTHING_TO_INLINE")

package me.squeezymo.spotlight.overlay.ext

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

internal inline fun View.offsetRectToAncestorCoords(ancestor: ViewGroup): Rect {
    val rect = Rect()
    offsetRectToAncestorCoords(ancestor, rect)
    return rect
}

internal inline fun View.offsetRectToAncestorCoords(ancestor: ViewGroup, rect: Rect) {
    getDrawingRect(rect)
    ancestor.offsetDescendantRectToMyCoords(this, rect)
}
