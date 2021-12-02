package me.squeezymo.demo.ui.util

import android.app.Activity
import android.content.res.Resources
import android.view.View
import androidx.annotation.Px
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt

@Px
private fun Resources.dp(dp: Int): Int = (dp * displayMetrics.density).roundToInt()

@Px
fun Activity.dp(dp: Int): Int = resources.dp(dp)

@Px
fun Fragment.dp(dp: Int): Int = resources.dp(dp)

@Px
fun View.dp(dp: Int): Int = resources.dp(dp)
