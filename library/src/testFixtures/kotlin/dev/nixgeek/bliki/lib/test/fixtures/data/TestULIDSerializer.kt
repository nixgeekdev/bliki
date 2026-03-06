package dev.nixgeek.bliki.lib.test.fixtures.data

import dev.nixgeek.bliki.lib.data.ulid.ULIDSerializer

object TestULIDSerializer : ULIDSerializer {
    override fun <T> serialize(value: T): String = value.toString()

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(value: String): T = value as T
}
