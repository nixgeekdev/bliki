package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.lib.data.ulid.ULIDEntity
import dev.nixgeek.bliki.lib.data.ulid.ULIDEntityClass
import dev.nixgeek.bliki.service.domain.model.EntryEvent
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant

class RevisionEntity(id: EntityID<String>) : ULIDEntity<String>(id) {
    companion object : ULIDEntityClass<String, RevisionEntity>(RevisionTable)

    var entry by EntryEntity referencedOn RevisionTable.entryId
    var author by ProfileEntity referencedOn RevisionTable.authorId
    var diff: String by RevisionTable.diff
    var summary: String? by RevisionTable.summary
    var event: EntryEvent by RevisionTable.event
    var createdAt: Instant? by RevisionTable.createdAt

    val entryId: String = entry.id.value
    val authorId: String = author.id.value
}
