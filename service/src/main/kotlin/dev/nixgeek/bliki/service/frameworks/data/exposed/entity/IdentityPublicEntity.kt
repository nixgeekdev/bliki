package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityPublicView
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class IdentityPublicEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, IdentityPublicEntity>(IdentityPublicView)

    var email: String by IdentityPublicView.email
    var createdAt: Instant by IdentityPublicView.createdAt
    var updatedAt: Instant by IdentityPublicView.updatedAt
}
