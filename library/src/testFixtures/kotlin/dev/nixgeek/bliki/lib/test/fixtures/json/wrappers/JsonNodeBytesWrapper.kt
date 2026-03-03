package dev.nixgeek.bliki.lib.test.fixtures.json.wrappers

import io.kotest.engine.names.WithDataTestName

data class JsonNodeBytesWrapper(
    val bytes: ByteArray,
) : WithDataTestName {
    override fun dataTestName(): String =
        "transforms ${bytes.size} bytes into json node"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsonNodeBytesWrapper

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}
