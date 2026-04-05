package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Tag(
    val id: ULID? = null,
    val parentId: ULID? = null,
    val term: String,
    val slug: String,
    val label: String? = null,
    val scheme: TagScheme? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun toPublicTag(): PublicTag =
        PublicTag(
            id = id!!,
            parentId = parentId,
            term = term,
            slug = slug,
            label = label,
            scheme = scheme?.type,
        )
}
