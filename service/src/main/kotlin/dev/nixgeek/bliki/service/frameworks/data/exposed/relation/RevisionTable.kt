package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import dev.nixgeek.bliki.service.domain.model.EntryEvent
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

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
    val createdAt = timestamp("created_at").default(Clock.System.now())
}
