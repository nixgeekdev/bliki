package dev.nixgeek.bliki.lib.extensions

import kotlin.time.Instant

fun Instant.toTimestamp(): Long = toEpochMilliseconds()
