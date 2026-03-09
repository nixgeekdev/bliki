package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.neq

object EntryRelationTable : CompositeIdTable("entry_relation") {
    private const val ENTRY_RELATION_COL_LEN = 16

    val fromEntryId = reference("from_entry_id", EntryTable.id).entityId()
    val toEntryId = reference("to_entry_id", EntryTable.id).entityId()
    val relation = enumerationByName(
        name = "relation",
        length = ENTRY_RELATION_COL_LEN,
        klass = EntryRelationType::class,
    ).default(EntryRelationType.RELATED)

    init {
        check("chk_entry_relation_no_self_reference") {
            fromEntryId neq toEntryId
        }
    }

    override val primaryKey = PrimaryKey(fromEntryId, toEntryId)
}
