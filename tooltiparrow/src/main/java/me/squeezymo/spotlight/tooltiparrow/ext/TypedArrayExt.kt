package me.squeezymo.spotlight.tooltiparrow.ext

import android.content.res.TypedArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalContracts
internal inline fun TypedArray.use(block: TypedArray.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        block(this)
    } finally {
        recycle()
    }
}
