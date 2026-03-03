package dev.nixgeek.bliki.lib.test.fixtures.json.wrappers

import io.kotest.engine.names.WithDataTestName

data class JsonNodeGetOrDefaultStringWrapper(
    val jsonString: String,
    val expected: String,
) : WithDataTestName {
    override fun dataTestName(): String =
        "get value based on path from $jsonString or default string"
}
