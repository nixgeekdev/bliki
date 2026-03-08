package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.PublicTag
import ulid.ULID

data class TagResponse(
    val id: ULID,
    val tag: PublicTag,
    val entries: List<EntryResponse>,
)
