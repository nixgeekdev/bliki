package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class SecureIdentity(
    val id: ULID,
    val email: String,
    val passwordHash: String,
)
