package me.squeezymo.demo.ui.util

import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.squeezymo.spotlight.overlay.AccentParams
import me.squeezymo.spotlight.overlay.SpotlightController
import me.squeezymo.spotlight.overlay.Tooltip

fun FloatingActionButton.expandMenu(
    shouldExpand: Boolean,
    tooltipByMenuItem: Map<View, Tooltip>,
    @LayoutRes menuCollapsedResId: Int,
    @LayoutRes menuExpandedResId: Int,
    spotlightController: SpotlightController,
    vararg menuItems: View
): Boolean {
    val constraintLayout = parent

    require(constraintLayout is ConstraintLayout) {
        "${ConstraintLayout::class.java.simpleName} expected as a parent but was ${constraintLayout::class.java.canonicalName}"
    }

    val constraintSet = ConstraintSet().apply {
        load(
            context,
            if (shouldExpand) menuExpandedResId else menuCollapsedResId
        )
    }

    val transition = TransitionSet().apply {
        duration = 250
        interpolator = AccelerateInterpolator()

        addTransition(
            AutoTransition()
        )

        addTransition(
            ChangeTransform().apply {
                addTarget(this@expandMenu)
            }
        )

        addListener(
            object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {
                    if (shouldExpand) {
                        val menuFab = this@expandMenu

                        spotlightController.show(
                            srcView = menuFab,
                            accents = HashMap<View, AccentParams>().apply {
                                if (tooltipByMenuItem.isEmpty()) {
                                    put(
                                        menuFab,
                                        AccentParams(
                                            invalidateAccentOnRedraws = true
                                        )
                                    )
                                }

                                menuItems.associateTo(this) { menuItem ->
                                    menuItem to AccentParams(
                                        invalidateAccentOnRedraws = true,
                                        tooltip = tooltipByMenuItem[menuItem]
                                    )
                                }
                            }
                        )
                    } else {
                        spotlightController.hide()
                    }
                }

                override fun onTransitionResume(transition: Transition) {
                    /* do nothing */
                }

                override fun onTransitionPause(transition: Transition) {
                    /* do nothing */
                }

                override fun onTransitionEnd(transition: Transition) {
                    /* do nothing */
                }

                override fun onTransitionCancel(transition: Transition) {
                    /* do nothing */
                }
            }
        )
    }

    TransitionManager.beginDelayedTransition(constraintLayout, transition)
    constraintSet.applyTo(constraintLayout)

    return shouldExpand
}
