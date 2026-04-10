package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.neq

/**
 * Exposed table definition for the `entry_relation` junction table.
 *
 * This table represents many-to-many relationships between entries (blog posts or wiki pages).
 * It allows entries to be linked to other entries with specific relationship types, enabling
 * features like "related posts", "see also" links, or hierarchical content organization.
 *
 * ## Table Structure
 * - Composite primary key: (`from_entry_id`, `to_entry_id`)
 * - Foreign keys to [EntryTable] for both source and target entries
 * - Enumerated `relation` column specifying the type of relationship
 *
 * ## Constraints
 * - **Self-reference prevention**: The table enforces that an entry cannot be related to itself
 *   via the `chk_entry_relation_no_self_reference` check constraint
 * - **Primary key uniqueness**: Each pair of entries can only have one relationship record
 *
 * ## Relationship Types
 * The `relation` column uses [EntryRelationType] enum values to classify relationships:
 * - `RELATED`: General related content
 * - Other types as defined in [EntryRelationType]
 *
 * ## Usage Example
 * ```kotlin
 * // Create a relation from entry A to entry B
 * EntryRelationTable.insert {
 *     it[fromEntryId] = entryA.id
 *     it[toEntryId] = entryB.id
 *     it[relation] = EntryRelationType.RELATED
 * }
 * ```
 *
 * @see EntryTable
 * @see EntryRelationType
 */
object EntryRelationTable : CompositeIdTable("entry_relation") {
    private const val ENTRY_RELATION_COL_LEN = 16

    val fromEntryId =
        reference(
            name = "from_entry_id",
            refColumn = EntryTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )

    val toEntryId =
        reference(
            name = "to_entry_id",
            refColumn = EntryTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )

    val relation =
        enumerationByName(
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
