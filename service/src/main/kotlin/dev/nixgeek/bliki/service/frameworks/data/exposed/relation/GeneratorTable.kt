package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object GeneratorTable : AbstractULIDTable("generator") {
    val name = text("name")
    val version = text("version")
    val uri = text("uri").nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}
