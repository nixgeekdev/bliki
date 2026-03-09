package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp

object IdentityPublicView : AbstractULIDTable("identity_public") {
    val email = text("email")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
