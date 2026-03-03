package dev.nixgeek.bliki.lib.test.fixtures.json.wrappers

import io.kotest.engine.names.WithDataTestName

data class JsonNodeGetOrNullWrapper(
    val jsonString: String,
    val expected: String? = null,
) : WithDataTestName {
    override fun dataTestName(): String =
        "get value based on path from $jsonString or null"
}
