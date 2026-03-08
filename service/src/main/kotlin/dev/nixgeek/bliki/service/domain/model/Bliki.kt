package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Bliki(
    val id: ULID? = null,
    val title: String,
    val subtitle: String? = null,
    val rights: String,
    val baseUri: String,
    val iconUri : String? = null,
    val logoUri: String? = null,
    val lang: String,
    val authorId: ULID,
    val generatorId: ULID,
    val updatedAt: Instant?,
)
