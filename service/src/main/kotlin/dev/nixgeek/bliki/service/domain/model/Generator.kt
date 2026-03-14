package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Generator(
    val id: ULID? = null,
    val name: String,
    val version: String,
    val uri: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
