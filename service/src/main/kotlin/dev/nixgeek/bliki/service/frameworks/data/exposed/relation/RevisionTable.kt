package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.service.domain.model.EntryEvent
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed ORM table definition for the `revision` table in the Bliki database schema.
 *
 * This table stores the revision history of blog entries, tracking all changes made to entries over time.
 * Each revision captures the diff (changes), a summary of what was modified, the event type, and timestamps.
 * Revisions are linked to both the entry being modified and the author (profile) who made the change.
 *
 * ## Relationships
 * - **Entry**: Each revision is associated with a single entry via `entryId` foreign key (RESTRICT on delete)
 * - **Profile**: Each revision is authored by a profile via `authorId` foreign key (RESTRICT on delete)
 *
 * ## Columns
 * - `id`: Primary key (ULID) inherited from [AbstractULIDTable]
 * - `entryId`: Foreign key reference to [EntryTable.id], represents the entry being revised
 * - `authorId`: Foreign key reference to [ProfileTable.id], represents the author of the revision
 * - `diff`: Text field containing the diff/changes made in this revision
 * - `summary`: Optional text field providing a human-readable summary of the changes
 * - `event`: Enumeration of type [EntryEvent] indicating the type of change (CREATED, UPDATED, etc.)
 * - `createdAt`: Timestamp of when the revision was created, defaults to current system time
 *
 * @see EntryTable
 * @see ProfileTable
 * @see EntryEvent
 * @see AbstractULIDTable
 */
object RevisionTable : AbstractULIDTable("revision") {
    private const val ENTRY_EVENT_COL_LEN = 10

    val entryId =
        reference(
            name = "entry_id",
            refColumn = EntryTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val authorId =
        reference(
            name = "author_id",
            refColumn = ProfileTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val diff = text("diff")
    val summary = text("summary").nullable()
    val event =
        enumerationByName(
            name = "event",
            length = ENTRY_EVENT_COL_LEN,
            klass = EntryEvent::class,
        ).default(EntryEvent.CREATED)
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())
}
