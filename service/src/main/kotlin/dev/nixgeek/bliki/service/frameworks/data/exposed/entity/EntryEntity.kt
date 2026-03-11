package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.domain.model.EntryStatus
import dev.nixgeek.bliki.service.domain.model.EntryVisibility
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class EntryEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, EntryEntity>(EntryTable)

    var bliki by BlikiEntity referencedOn EntryTable.blikiId
    var author by ProfileEntity referencedOn EntryTable.authorId
    var title: String by EntryTable.title
    var slug: String by EntryTable.slug
    var content: String by EntryTable.content
    var summary: String? by EntryTable.summary
    var lang: String by EntryTable.lang
    var contentType: String by EntryTable.contentType
    var visibility: EntryVisibility by EntryTable.visibility
    var status: EntryStatus by EntryTable.status
    var publishedAt: Instant? by EntryTable.publishedAt
    var createdAt: Instant by EntryTable.createdAt
    var updatedAt: Instant by EntryTable.updatedAt

    val blikiId: String = bliki.id.value
    val authorId: String = author.id.value
}
