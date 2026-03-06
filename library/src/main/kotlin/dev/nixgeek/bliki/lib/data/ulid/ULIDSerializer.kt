package dev.nixgeek.bliki.lib.data.ulid

interface ULIDSerializer {
    fun <T> serialize(value: T): String
    fun <T> deserialize(value: String): T
}
