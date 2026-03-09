package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

object EntryTagTable : CompositeIdTable("entry_tag") {
    val entryId = reference("entry_id", EntryTable.id).entityId()
    val tagId = reference("tag_id", TagTable.id).entityId()

    override val primaryKey = PrimaryKey(entryId, tagId)
}
