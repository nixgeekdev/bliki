package dev.nixgeek.bliki.service.frameworks.data.exposed.entity

import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.EntryContributorTable
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class EntryContributorEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<EntryContributorEntity>(EntryContributorTable)

    var entry by EntryEntity referencedOn EntryContributorTable.entryId
    var contributor by ProfileEntity referencedOn EntryContributorTable.profileId

    val entryId: String = entry.id.value
    val profileId: String = contributor.id.value
}
