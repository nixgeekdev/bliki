package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class RoleEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, RoleEntity>(RoleTable)

    var role: String by RoleTable.role
    var label: String by RoleTable.label
    var createdAt: Instant? by RoleTable.createdAt
    var updatedAt: Instant? by RoleTable.updatedAt
}
