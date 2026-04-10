package dev.nixgeek.bliki.lib.extensions

import java.time.OffsetDateTime
import kotlin.time.Instant
import kotlin.time.toKotlinInstant

fun Instant.toTimestamp(): Long = toEpochMilliseconds()

fun OffsetDateTime.toKotlinInstant(): Instant = toInstant().toKotlinInstant()
