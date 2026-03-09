package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object RoleTable : AbstractULIDTable("roles") {
    val role = text("role")
    val label = text("label")
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
}
