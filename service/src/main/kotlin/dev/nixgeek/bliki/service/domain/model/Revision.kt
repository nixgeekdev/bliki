package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Revision(
    val id: ULID? = null,
    val entryId: ULID,
    val authorId: ULID,
    val diff: String,
    val summary: String? = null,
    val event: EntryEvent = EntryEvent.CREATED,
    val createdAt: Instant? = null,
)
