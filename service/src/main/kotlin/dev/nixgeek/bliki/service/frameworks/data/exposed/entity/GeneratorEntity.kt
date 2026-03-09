package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class GeneratorEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, GeneratorEntity>(GeneratorTable)

    var name: String by GeneratorTable.name
    var version: String by GeneratorTable.version
    var uri: String? by GeneratorTable.uri
    var createdAt: Instant by GeneratorTable.createdAt
    var updatedAt: Instant by GeneratorTable.updatedAt
}
