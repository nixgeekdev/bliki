package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class PublicRole(
    val id: ULID,
    val role: IdentityRole,
    val label: String,
)
