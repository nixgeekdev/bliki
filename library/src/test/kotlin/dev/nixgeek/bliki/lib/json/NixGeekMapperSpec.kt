package dev.nixgeek.bliki.lib.json

import dev.nixgeek.bliki.lib.test.fixtures.json.model.Foo
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import tools.jackson.databind.JsonNode
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import kotlin.time.Instant

class NixGeekMapperSpec : FunSpec({
    context("instant should serde") {
        withData(
            "2019-11-10T09:11:01.758581Z",
            "2020-05-14T12:07:41.159357Z",
            "2021-04-14T06:06:44.963147Z",
            "2022-09-09T09:08:45.852258Z",
        ) { value ->
            val json = NixGeekMapper.mapper.writeValueAsString(Instant.parse(value))
            val deserInstant = NixGeekMapper.mapper.readValue(json, Instant::class.java)
            Instant.parse(value) shouldBe deserInstant
        }
    }

    context("simple serde") {
        test("simple data class serialize") {
            val json =
                NixGeekMapper.mapper
                    .writeValueAsString(
                        Foo("test", 1, Instant.parse("2022-01-01T00:00:00.000Z")),
                    )
            json shouldBe "{\"bar\":\"test\",\"baz\":1,\"qux\":\"2022-01-01T00:00:00Z\"}"
        }

        test("simple data class deserialize") {
            val foo = Foo("QWERTY", 2, Instant.parse("2022-02-02T00:00:00.000Z"))
            val node = NixGeekMapper.mapper.valueToTree<JsonNode>(foo)
            foo shouldBe NixGeekMapper.mapper.treeToValue(node, Foo::class.java)
        }
    }

    context("snake case strategy regression") {
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
        data class Testing(val one: String, val oneTwo: String, val twoThree: String)

        test("snake case serialization") {
            val testing = Testing("val1", "val2", "val3")
            val json = NixGeekMapper.mapper.writeValueAsString(testing)
            json shouldBe "{\"one\":\"val1\",\"one_two\":\"val2\",\"two_three\":\"val3\"}"
        }
    }
})
