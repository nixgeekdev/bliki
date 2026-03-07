package dev.nixgeek.bliki.lib.data.ulid

import ulid.ULID

fun String.toULID(): ULID = ULID.StatefulMonotonic().parseULID(this)

fun ByteArray.toULID(): ULID = ULID.StatefulMonotonic().fromBytes(this)
