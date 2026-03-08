package dev.nixgeek.bliki.service.domain.model.request

import dev.nixgeek.bliki.service.domain.model.EntryContentType
import dev.nixgeek.bliki.service.domain.model.EntryStatus
import dev.nixgeek.bliki.service.domain.model.EntryVisibility
import kotlin.time.Instant

data class SaveEntryRequest(
    val id: String? = null,
    val blikiId: String,
    val title: String,
    val slug: String,
    val content: String,
    val summary: String? = null,
    val lang: String,
    val contentType: EntryContentType? = null,
    val authorId: String,
    val visibility: EntryVisibility? = null,
    val status: EntryStatus,
    val publishedAt: Instant? = null,
) {
    val isUpdatable: Boolean = id != null

    val isPublished: Boolean =
        status == EntryStatus.PUBLISHED && publishedAt != null
}
