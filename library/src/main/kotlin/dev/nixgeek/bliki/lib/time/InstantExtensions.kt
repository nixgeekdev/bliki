package dev.nixgeek.bliki.lib.time

import kotlin.time.Instant

fun Instant.toTimestamp(): Long = toEpochMilliseconds()
