package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

object EntryContributorTable : CompositeIdTable("entry_contributor") {
    val entryId = reference("entry_id", EntryTable.id).entityId()
    val profileId = reference("profile_id", ProfileTable.id).entityId()

    override val primaryKey = PrimaryKey(entryId, profileId)
}
