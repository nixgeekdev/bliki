package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class SecureRole(
    val id: ULID,
    val role: IdentityRole,
)
