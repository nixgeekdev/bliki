package dev.nixgeek.bliki.service.domain.model.request

import dev.nixgeek.bliki.service.domain.model.EntryEvent
import ulid.ULID

data class SaveRevisionRequest(
    val id: ULID? = null,
    val entryId: ULID,
    val authorId: ULID,
    val diff: String,
    val summary: String,
    val event: EntryEvent,
) {
    val isUpdatable: Boolean = id != null
}
