package me.squeezymo.spotlight.overlay.ext

import androidx.fragment.app.Fragment
import me.squeezymo.spotlight.overlay.SpotlightController

val Fragment.spotlightController: SpotlightController
    get() = requireActivity().spotlightController
