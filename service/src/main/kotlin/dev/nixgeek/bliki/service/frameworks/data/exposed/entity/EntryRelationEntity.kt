package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryRelationTable
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class EntryRelationEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<EntryRelationEntity>(EntryRelationTable)

    var fromEntry by EntryEntity referencedOn EntryRelationTable.fromEntryId
    var toEntry by EntryEntity referencedOn EntryRelationTable.toEntryId
    var relation: EntryRelationType by EntryRelationTable.relation

    val fromEntryId: String = fromEntry.id.value
    val toEntryId: String = toEntry.id.value
}
