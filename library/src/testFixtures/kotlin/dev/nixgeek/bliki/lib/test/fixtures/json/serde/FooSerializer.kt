package dev.nixgeek.bliki.lib.test.fixtures.json.serde

import dev.nixgeek.bliki.lib.test.fixtures.json.model.Foo
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer

class FooSerializer : StdSerializer<Foo>(Foo::class.java) {
    override fun serialize(
        value: Foo,
        gen: JsonGenerator,
        provider: SerializationContext,
    ) {
        with(gen) {
            writeStartObject()
            writeStringProperty("bar", value.bar)
            writeNumberProperty("baz", value.baz)
            writeStringProperty("qux", value.qux.toString())
            writeEndObject()
        }
    }
}
