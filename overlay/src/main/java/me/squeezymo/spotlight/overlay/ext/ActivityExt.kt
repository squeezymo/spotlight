package me.squeezymo.spotlight.overlay.ext

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import me.squeezymo.spotlight.overlay.SpotlightController
import java.util.*

fun ComponentActivity.setContentViewWithOverlay(@LayoutRes layoutResID: Int) {
    setContentViewWithOverlay(layoutInflater.inflate(layoutResID, null))
}

fun ComponentActivity.setContentViewWithOverlay(view: View) {
    setContentView(view)

    val parent = view.parent

    val contentView = if (parent is FrameLayout) {
        parent
    } else {
        Log.w(
            SpotlightController::class.java.simpleName,
            "Unexpected root parent: ${parent::class.java.canonicalName}. View hierarchy will be one level deeper"
        )

        (parent as? ViewGroup)?.removeView(view)
        FrameLayout(this).also { root ->
            root.addView(view)
            setContentView(root)
        }
    }

    controllers[this] = SpotlightController(contentView)
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                controllers.remove(owner)
            }
        }
    )
}

val ComponentActivity.spotlightController: SpotlightController
    get() = controllers[this]
        ?: throw IllegalStateException(
            "This activity is either not yet created (or created without a call to setContentViewWithOverlay()) " +
                    "or already destroyed"
        )

private val controllers: MutableMap<LifecycleOwner, SpotlightController> = WeakHashMap()
