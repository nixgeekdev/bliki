package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object ProfileTable : AbstractULIDTable("profile") {
    val identityId =
        reference(
            name = "identity_id",
            refColumn = IdentityTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val fullName = text("full_name")
    val affiliation = text("affiliation").nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}
