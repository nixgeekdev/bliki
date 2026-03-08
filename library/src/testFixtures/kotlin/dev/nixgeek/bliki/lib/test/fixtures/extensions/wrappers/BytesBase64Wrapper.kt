package dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers

import io.kotest.engine.names.WithDataTestName

data class BytesBase64Wrapper(
    val bytes: ByteArray,
    val encoded: String,
) : WithDataTestName {
    override fun dataTestName(): String =
        "base64(${bytes.size} bytes) -> $encoded"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BytesBase64Wrapper

        if (!bytes.contentEquals(other.bytes)) return false
        if (encoded != other.encoded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + encoded.hashCode()
        return result
    }
}
