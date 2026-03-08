package dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers

import io.kotest.engine.names.WithDataTestName

data class BytesChunkedWrapper(
    val value: ByteArray,
    val chunkSize: Int,
    val expected: List<ByteArray>,
) : WithDataTestName {
    override fun dataTestName(): String =
        "chunked($chunkSize) of ${value.size} bytes"
}
