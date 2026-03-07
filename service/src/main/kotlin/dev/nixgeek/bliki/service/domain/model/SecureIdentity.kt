package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class SecureIdentity(
    val id: ULID,
    val email: String,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
