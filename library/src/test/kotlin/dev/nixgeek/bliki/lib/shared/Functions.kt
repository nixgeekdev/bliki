package dev.nixgeek.bliki.lib.shared

import kotlin.time.TimeSource

/**
 * Executes the provided lambda and returns the result along with the time
 * taken in milliseconds.
 */
inline fun <reified T> timedMs(fn: () -> T): Pair<T, Long> =
    TimeSource.Monotonic.markNow().let { mark ->
        fn() to mark.elapsedNow().inWholeMilliseconds
    }
