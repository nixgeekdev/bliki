package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class ProfileEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, ProfileEntity>(ProfileTable)

    var identity by IdentityEntity referencedOn ProfileTable.identityId
    var fullName: String by ProfileTable.fullName
    var affiliation: String? by ProfileTable.affiliation
    var createdAt: Instant by ProfileTable.createdAt
    var updatedAt: Instant by ProfileTable.updatedAt

    val identityId: String = identity.id.value
}
