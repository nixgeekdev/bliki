package dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers

import io.kotest.engine.names.WithDataTestName

data class StringHexWrapper(
    val hexString: String,
    val expected: ByteArray,
) : WithDataTestName {
    override fun dataTestName(): String =
        "decodes $hexString into $expected"
}
