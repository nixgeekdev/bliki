package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class EntryRelation(
    val fromEntryId: ULID,
    val toEntryId: ULID,
    val relation: EntryRelationType = EntryRelationType.RELATED,
)
