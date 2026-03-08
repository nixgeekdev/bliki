package dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers

import io.kotest.engine.names.WithDataTestName

data class StringBase64Wrapper(
    val base64String: String,
    val expected: ByteArray,
) : WithDataTestName {
    override fun dataTestName(): String =
        "decodes $base64String into $expected"
}
