package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.model.TagScheme
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.TagTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import kotlin.time.Instant

class TagEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, TagEntity>(TagTable)

    var parent: TagEntity? by TagEntity optionalReferencedOn TagTable.parentId
    var term: String by TagTable.term
    var slug: String by TagTable.slug
    var label: String by TagTable.label
    var scheme: TagScheme? by TagTable.scheme
    var createdAt: Instant by TagTable.createdAt
    var updatedAt: Instant by TagTable.updatedAt

    val parentId: String? = parent?.id?.value
    val children: SizedIterable<TagEntity> by TagEntity optionalReferrersOn TagTable.parentId
}
