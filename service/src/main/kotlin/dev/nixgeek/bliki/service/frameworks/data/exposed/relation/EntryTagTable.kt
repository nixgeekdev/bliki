package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

/**
 * Junction table representing the many-to-many relationship between entries and tags.
 *
 * This table implements the association between [EntryTable] and [TagTable], allowing
 * each entry to have multiple tags and each tag to be associated with multiple entries.
 *
 * The table uses a composite primary key consisting of both foreign keys, ensuring that
 * each entry-tag pairing is unique and preventing duplicate associations.
 *
 * Database schema reference: See `V001__Bliki_Schema.sql` for the corresponding SQL DDL.
 *
 * @see EntryTable
 * @see TagTable
 */
object EntryTagTable : CompositeIdTable("entry_tag") {
    /**
     * Foreign key reference to the entry in the entry-tag relationship.
     * Part of the composite primary key.
     */
    val entryId =
        reference(
            name = "entry_id",
            refColumn = EntryTable.id,
            onDelete = ReferenceOption.RESTRICT
        ).entityId()

    /**
     * Foreign key reference to the tag in the entry-tag relationship.
     * Part of the composite primary key.
     */
    val tagId =
        reference(
            name = "tag_id",
            refColumn = TagTable.id,
            onDelete = ReferenceOption.RESTRICT
        ).entityId()

    /**
     * Composite primary key constraint ensuring each entry-tag pairing is unique.
     */
    override val primaryKey = PrimaryKey(entryId, tagId)
}
