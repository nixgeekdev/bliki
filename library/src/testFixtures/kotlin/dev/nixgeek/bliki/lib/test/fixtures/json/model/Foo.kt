package dev.nixgeek.bliki.lib.test.fixtures.json.model

import dev.nixgeek.bliki.lib.test.fixtures.json.serde.FooDeserializer
import dev.nixgeek.bliki.lib.test.fixtures.json.serde.FooSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import kotlin.time.Clock
import kotlin.time.Instant

@JsonSerialize(using = FooSerializer::class)
@JsonDeserialize(using = FooDeserializer::class)
data class Foo(
    val bar: String,
    val baz: Int,
    val qux: Instant? = Clock.System.now(),
)
