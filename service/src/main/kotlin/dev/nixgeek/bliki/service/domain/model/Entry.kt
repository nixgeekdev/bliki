package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Entry(
    val id: ULID? = null,
    val blikiId: ULID,
    val title: String,
    val slug: String,
    val content: String,
    val summary: String? = null,
    val lang: String,
    val contentType: EntryContentType = EntryContentType.MARKDOWN,
    val authorId: ULID,
    val visibility: EntryVisibility = EntryVisibility.PRIVATE,
    val status: EntryStatus = EntryStatus.DRAFT,
    val publishedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
