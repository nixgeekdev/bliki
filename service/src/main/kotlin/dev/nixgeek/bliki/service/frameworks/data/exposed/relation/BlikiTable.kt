package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object BlikiTable : AbstractULIDTable("bliki") {
    val title = text("title")
    val subtitle = text("subtitle").nullable()
    val rights = text("rights")
    val baseUri = text("base_uri")
    val iconUri = text("icon_uri").nullable()
    val logoUri = text("logo_uri").nullable()
    val lang = text("lang")
    val authorId =
        reference(
            name = "author_id",
            refColumn = IdentityTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val generatorId =
        reference(
            name = "generator_id",
            refColumn = GeneratorTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())
}
