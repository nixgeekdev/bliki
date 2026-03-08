package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Tag(
    val id: ULID? = null,
    val term: String,
    val slug: String,
    val label: String? = null,
    val scheme: String? = null,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    fun toPublicTag(): PublicTag =
        PublicTag(
            id = id!!,
            term = term,
            slug = slug,
            label = label,
            scheme = scheme,
        )
}
