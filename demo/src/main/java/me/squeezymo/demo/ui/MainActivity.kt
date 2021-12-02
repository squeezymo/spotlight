package me.squeezymo.demo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.squeezymo.demo.R
import me.squeezymo.demo.ui.contract.FabMenuOwner
import me.squeezymo.demo.ui.util.createTooltip
import me.squeezymo.demo.ui.util.dp
import me.squeezymo.demo.ui.util.expandMenu
import me.squeezymo.spotlight.overlay.OverlayTouchHandlingStrategy
import me.squeezymo.spotlight.overlay.ext.setContentViewWithOverlay
import me.squeezymo.spotlight.overlay.ext.spotlightController
import me.squeezymo.spotlight.overlay.internal.view.TooltipsViewGroup

class MainActivity : AppCompatActivity(), FabMenuOwner {

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var menuFab: FloatingActionButton
    private lateinit var contactUsFab: FloatingActionButton
    private lateinit var logOutFab: FloatingActionButton

    private var isFabMenuExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithOverlay(R.layout.a_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        spotlightController.setTouchHandlingStrategy(
            OverlayTouchHandlingStrategy.NonAccentedClicksListener {
                if (isFabMenuExpanded) {
                    expandMenu(!isFabMenuExpanded)
                } else {
                    Toast.makeText(this, "Overlay clicked", Toast.LENGTH_SHORT).show()
                }
            }
        )

        constraintLayout = findViewById(R.id.constraint_layout)
        menuFab = constraintLayout.findViewById(R.id.menu_fab)
        contactUsFab = constraintLayout.findViewById(R.id.contact_us_fab)
        logOutFab = constraintLayout.findViewById(R.id.log_out_fab)

        menuFab.setOnClickListener {
            expandMenu(!isFabMenuExpanded)
        }
    }

    override fun getFloatingActionMenu(): FloatingActionButton {
        return menuFab
    }

    override fun expandMenu(shouldExpand: Boolean, isTourMode: Boolean): Boolean {
        isFabMenuExpanded = menuFab.expandMenu(
            shouldExpand = shouldExpand,
            tooltipByMenuItem = if (isTourMode) {
                mapOf(
                    contactUsFab to spotlightController.createTooltip(
                        anchor = contactUsFab,
                        text = "Reach us via email",
                        distancePx = dp(16),
                        manualBearing = TooltipsViewGroup.LayoutParams.BEARING_LEFT_DEGREES,
                        showArrow = false
                    ),
                    logOutFab to spotlightController.createTooltip(
                        anchor = logOutFab,
                        text = "Log out of your account",
                        distancePx = dp(16),
                        manualBearing = TooltipsViewGroup.LayoutParams.BEARING_LEFT_DEGREES,
                        showArrow = false
                    )
                )
            } else {
                emptyMap()
            },
            menuCollapsedResId = R.layout.l_fab_menu_collapsed,
            menuExpandedResId = R.layout.l_fab_menu_expanded,
            spotlightController = spotlightController,
            contactUsFab,
            logOutFab
        )

        return isFabMenuExpanded
    }

}
