package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.lib.data.ulid.ULIDSerializer

object StringULIDSerializer : ULIDSerializer {
    override fun <T> serialize(value: T): String = value.toString()

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(value: String): T = value as T
}
