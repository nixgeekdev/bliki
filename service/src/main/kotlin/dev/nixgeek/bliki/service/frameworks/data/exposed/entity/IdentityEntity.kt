package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class IdentityEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, IdentityEntity>(IdentityTable)

    var email: String by IdentityTable.email
    var passwordHash: String by IdentityTable.passwordHash
    var createdAt: Instant? by IdentityTable.createdAt
    var updatedAt: Instant? by IdentityTable.updatedAt
}
