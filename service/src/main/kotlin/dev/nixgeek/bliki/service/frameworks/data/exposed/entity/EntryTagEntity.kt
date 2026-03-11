package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryTagTable
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class EntryTagEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<EntryTagEntity>(EntryTagTable)

    var entry by EntryEntity referencedOn EntryTagTable.entryId
    var tag by TagEntity referencedOn EntryTagTable.tagId

    val entryId: String = entry.id.value
    val tagId: String = tag.id.value
}
