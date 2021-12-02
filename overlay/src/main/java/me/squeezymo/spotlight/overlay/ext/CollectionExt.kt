@file:Suppress("NOTHING_TO_INLINE")

package me.squeezymo.spotlight.overlay.ext

internal inline fun <K, V, R> Map<K, V?>.mapNotNullValuesTo(
    destination: MutableMap<K, R>,
    mapper: (K, V) -> R?
): Map<K, R> {
    for ((key, value) in this) {
        if (value != null) {
            val mappedValue = mapper(key, value)
            if (mappedValue != null) {
                destination[key] = mappedValue
            }
        }
    }

    return destination
}

internal inline fun <K, V, R> Map<K, V?>.mapNotNullValues(
    mapper: (K, V) -> R?
): Map<K, R> {
    return mapNotNullValuesTo(LinkedHashMap(), mapper)
}
