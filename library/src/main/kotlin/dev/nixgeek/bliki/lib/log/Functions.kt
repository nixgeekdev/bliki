package dev.nixgeek.bliki.lib.log

import kotlin.time.TimeSource

fun buildLogMessage(message: String?, vararg values: Pair<String, Any?>): String =
    buildString {
        val hasList = values.isNotEmpty()
        if (!message.isNullOrBlank()) {
            append(message)
            if (hasList) {
                append(" ")
            }
        }
        if (hasList) {
            append("[")
            append(values.joinToString { (name, value) -> "$name: $value" })
            append("]")
        }
    }

/**
 * Executes the provided lambda and returns the result along with the time
 * taken in milliseconds.
 */
inline fun <reified T> timedMs(fn: () -> T): Pair<T, Long> =
    TimeSource.Monotonic.markNow().let { mark ->
        fn() to mark.elapsedNow().inWholeMilliseconds
    }
