package dev.nixgeek.bliki.lib.test.fixtures.json.wrappers

import io.kotest.engine.names.WithDataTestName
import tools.jackson.databind.JsonNode

data class JsonNodeGetOrDefaultNodeWrapper(
    val jsonString: String,
    val expected: JsonNode,
) : WithDataTestName {
    override fun dataTestName(): String =
        "get value based on path from $jsonString or default node"
}
