package dev.nixgeek.bliki.lib.test.fixtures.data.ulid

import dev.nixgeek.bliki.lib.data.ulid.ULIDTable
import dev.nixgeek.bliki.lib.data.ulid.ulid

object TestUlidTable : ULIDTable<String>(
    name = "test_ulid_table",
    serializer = TestULIDSerializer,
    ulidGenerator = { "01KJYB1H3PVXCKRERQPWPKM9JG" },
) {
    val ulid = ulid<String>("ulid", TestULIDSerializer)
}
