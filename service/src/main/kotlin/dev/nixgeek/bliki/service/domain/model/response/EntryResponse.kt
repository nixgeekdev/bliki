package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelation
import dev.nixgeek.bliki.service.domain.model.PublicProfile
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.model.Tag
import ulid.ULID

data class EntryResponse(
    val id: ULID,
    val entry: Entry,
    val author: PublicProfile,
    val revisions: List<Revision>,
    val tags: List<Tag>,
    val relations: List<EntryRelation>,
    val contributors: List<PublicProfile>,
)
