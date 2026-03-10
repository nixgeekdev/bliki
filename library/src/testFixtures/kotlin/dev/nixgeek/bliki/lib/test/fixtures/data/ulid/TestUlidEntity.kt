package dev.nixgeek.bliki.lib.test.fixtures.data.ulid

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID

class TestUlidEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, TestUlidEntity>(TestUlidTable)

    var ulid by TestUlidTable.ulid
}
