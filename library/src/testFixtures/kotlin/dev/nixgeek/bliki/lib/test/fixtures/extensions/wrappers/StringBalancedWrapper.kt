package dev.nixgeek.bliki.lib.test.fixtures.extensions.wrappers

import io.kotest.engine.names.WithDataTestName

data class StringBalancedWrapper(
    val value: String,
    val expected: Boolean,
) : WithDataTestName {
    override fun dataTestName(): String =
        "checks whether ${value.ifEmpty { "<empty>" }} is balanced = $expected"
}
