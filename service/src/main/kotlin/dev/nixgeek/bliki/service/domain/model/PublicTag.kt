package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class PublicTag(
    val id: ULID,
    val parentId: ULID? = null,
    val term: String,
    val slug: String,
    val label: String? = null,
    val scheme: String? = null,
)
