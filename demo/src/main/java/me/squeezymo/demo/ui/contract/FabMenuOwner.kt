package me.squeezymo.demo.ui.contract

import com.google.android.material.floatingactionbutton.FloatingActionButton

interface FabMenuOwner {

    fun getFloatingActionMenu(): FloatingActionButton

    fun expandMenu(shouldExpand: Boolean, isTourMode: Boolean = false): Boolean

}
