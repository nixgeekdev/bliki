package dev.nixgeek.bliki.lib.test.fixtures.json.serde

import dev.nixgeek.bliki.lib.test.fixtures.json.model.Foo
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.deser.std.StdDeserializer
import kotlin.time.Instant

class FooDeserializer : StdDeserializer<Foo>(Foo::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): Foo? {
        val node = p.readValueAsTree<JsonNode>()
        return Foo(
            bar = node.get("bar").asString(),
            baz = node.get("baz").asInt(),
            qux = Instant.parse(node.get("qux").asString()),
        )
    }
}
