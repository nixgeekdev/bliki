package dev.nixgeek.bliki.lib.test.fixtures.data

import dev.nixgeek.bliki.lib.data.ulid.ULIDSerializer
import dev.nixgeek.bliki.lib.data.ulid.ULIDTable

object TestUlidTableWithCustomSerializer : ULIDTable<String>(
    serializer = TestUlidSerializer,
    ulidGenerator = { "01KJYBMHGBV2QTVXSHM5TAS0MB" },
)

object TestUlidSerializer : ULIDSerializer {
    override fun <T> serialize(value: T): String =
        value.toString().dropLast(1) + "S"

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(value: String): T = value as T
}
