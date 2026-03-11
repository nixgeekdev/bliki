package dev.nixgeek.bliki.lib.test.fixtures.json.wrappers

import io.kotest.engine.names.WithDataTestName

data class JsonNodeStringWrapper(
    val jsonString: String,
) : WithDataTestName {
    override fun dataTestName(): String =
        "transforms $jsonString into json node"
}
