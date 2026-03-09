package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.lib.data.ulid.ULIDTable
import ulid.ULID

abstract class AbstractULIDTable(name: String = "") : ULIDTable<String>(
    name = name,
    serializer = StringULIDSerializer,
    ulidGenerator = { ULID.StatefulMonotonic().nextULID().toString() },
)
