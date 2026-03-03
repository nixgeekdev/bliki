package dev.nixgeek.bliki.lib.json

import dev.nixgeek.bliki.lib.test.fixtures.json.JSON_RESOURCE_FILE
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_ADDRESS_EXPECTED
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_ADDRESS_EXPECTED_PRETTY
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_EMPTY
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_EXPECTED
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_PATH
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_RANDOM_001
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_RANDOM_002
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_RANDOM_003
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_RESOURCE_FILE
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_SPACE
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_USER_001
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_USER_002
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_WARN_BYTES
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_WARN_DATA
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_WARN_NODE
import dev.nixgeek.bliki.lib.test.fixtures.json.TEST_JSON_WARN_STRING
import dev.nixgeek.bliki.lib.test.fixtures.json.loadResourceData
import dev.nixgeek.bliki.lib.test.fixtures.json.model.USAddress
import dev.nixgeek.bliki.lib.test.fixtures.json.wrappers.JsonNodeBytesWrapper
import dev.nixgeek.bliki.lib.test.fixtures.json.wrappers.JsonNodeGetOrDefaultStringWrapper
import dev.nixgeek.bliki.lib.test.fixtures.json.wrappers.JsonNodeGetOrNullWrapper
import dev.nixgeek.bliki.lib.test.fixtures.json.wrappers.JsonNodeStringWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.engine.test.logging.warn
import io.kotest.matchers.shouldBe
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.JsonNode

@OptIn(ExperimentalKotest::class)
class JsonExtensionsSpec : FunSpec({
    test("get json from a string representing a resource file path") {
        val testData: String? = loadResourceData(TEST_JSON_RESOURCE_FILE)
        val jsonNode: JsonNode? = JSON_RESOURCE_FILE.loadAsJsonNode()
        warn { "$TEST_JSON_WARN_DATA $testData" }
        warn { "$TEST_JSON_WARN_NODE $jsonNode" }
        jsonNode shouldBe NixGeekMapper.mapper.readTree(testData)
    }

    context("transforms string into a json node") {
        withData(
            JsonNodeStringWrapper(TEST_JSON_USER_001),
            JsonNodeStringWrapper(TEST_JSON_RANDOM_001),
        ) { (jsonString) ->
            warn { "$TEST_JSON_WARN_STRING $jsonString" }
            warn { "$TEST_JSON_WARN_NODE ${jsonString.asJsonNode()}" }
            jsonString.asJsonNode() shouldBe NixGeekMapper.mapper.readTree(jsonString)
        }
    }

    context("transforms byte array into a json node") {
        withData(
            JsonNodeBytesWrapper(TEST_JSON_USER_002.toByteArray()),
            JsonNodeBytesWrapper(TEST_JSON_RANDOM_002.toByteArray()),
        ) { (bytes) ->
            warn { "$TEST_JSON_WARN_BYTES $bytes" }
            warn { "$TEST_JSON_WARN_NODE ${bytes.asJsonNode()}" }
            bytes.asJsonNode() shouldBe NixGeekMapper.mapper.readTree(bytes)
        }
    }

    context("get a json node value based on key or null") {
        withData(
            JsonNodeGetOrNullWrapper(TEST_JSON_USER_001, TEST_JSON_EXPECTED),
            JsonNodeGetOrNullWrapper(TEST_JSON_RANDOM_001, null),
        ) { (jsonString, expected) ->
            warn { "$TEST_JSON_WARN_STRING $jsonString" }
            warn { "$TEST_JSON_WARN_NODE ${jsonString.asJsonNode().getOrNull(TEST_JSON_PATH)?.asJson()}" }
            jsonString.asJsonNode().getOrNull(TEST_JSON_PATH)?.asPrettyJson() shouldBe
                expected?.let { "\"$expected\"" }
        }
    }

    context("get a json node value based on key or default node") {
        val default =
            NixGeekMapper.mapper
                .createObjectNode()
                .put(TEST_JSON_PATH, TEST_JSON_SPACE)
                .get(TEST_JSON_PATH)
        withData(
            JsonNodeGetOrDefaultStringWrapper(TEST_JSON_USER_001, TEST_JSON_EXPECTED),
            JsonNodeGetOrDefaultStringWrapper(TEST_JSON_RANDOM_001, TEST_JSON_SPACE),
        ) { (jsonString, expected) ->
            warn { "$TEST_JSON_WARN_STRING $jsonString" }
            warn {
                "$TEST_JSON_WARN_NODE ${jsonString.asJsonNode().getOrDefault(TEST_JSON_PATH, default).asJson()}"
            }
            jsonString.asJsonNode().getOrDefault(TEST_JSON_PATH, default).asPrettyJson() shouldBe "\"$expected\""
        }
    }

    context("get a json node value based on key or default string") {
        withData(
            JsonNodeGetOrDefaultStringWrapper(TEST_JSON_USER_001, TEST_JSON_EXPECTED),
            JsonNodeGetOrDefaultStringWrapper(TEST_JSON_RANDOM_001, TEST_JSON_EMPTY),
        ) { (jsonString, expected) ->
            warn { "$TEST_JSON_WARN_STRING $jsonString" }
            warn { "$TEST_JSON_WARN_NODE ${jsonString.asJsonNode().getOrDefault(TEST_JSON_PATH).asJson()}" }
            jsonString.asJsonNode().getOrDefault(TEST_JSON_PATH).asPrettyJson() shouldBe "\"$expected\""
        }
    }

    context("generic tests") {
        test("transforms any kotlin object into a json string") {
            USAddress(
                id = 1,
                street1 = "100 West Liberty St",
                city = "Reno",
                state = "NV",
                zip = "89501",
                street2 = "suite 600",
            ).asJson() shouldBe TEST_JSON_ADDRESS_EXPECTED
        }

        test("transforms any kotlin object into a pretty json string") {
            USAddress(
                id = 1,
                street1 = "100 West Liberty St",
                city = "Reno",
                state = "NV",
                zip = "89501",
                street2 = "suite 600",
            ).asPrettyJson() shouldBe TEST_JSON_ADDRESS_EXPECTED_PRETTY
        }

        test("transforms any kotlin object into a json byte array") {
            USAddress(
                id = 1,
                street1 = "100 West Liberty St",
                city = "Reno",
                state = "NV",
                zip = "89501",
                street2 = "suite 600",
            ).asJsonBytes() shouldBe TEST_JSON_ADDRESS_EXPECTED.toByteArray()
        }

        test("transforms a json node into a kotlin object") {
            TEST_JSON_ADDRESS_EXPECTED.asJsonNode().asType<USAddress>() shouldBe
                USAddress(
                    id = 1,
                    street1 = "100 West Liberty St",
                    city = "Reno",
                    state = "NV",
                    zip = "89501",
                    street2 = "suite 600",
                )
        }

        test("transforms a kotlin object into a json node") {
            USAddress(
                id = 1,
                street1 = "100 West Liberty St",
                city = "Reno",
                state = "NV",
                zip = "89501",
                street2 = "suite 600",
            ).asJsonNode() shouldBe TEST_JSON_ADDRESS_EXPECTED.asJsonNode()
        }

        test("transforms a string into a kotlin object") {
            TEST_JSON_ADDRESS_EXPECTED.asType<USAddress>() shouldBe
                USAddress(
                    id = 1,
                    street1 = "100 West Liberty St",
                    city = "Reno",
                    state = "NV",
                    zip = "89501",
                    street2 = "suite 600",
                )
        }

        test("on string to json node transformation failure, throw") {
            shouldThrow<StreamReadException> { TEST_JSON_RANDOM_003.asJsonNode() }
        }
    }
})
