package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class PublicIdentity(
    val id: ULID,
    val email: String,
)
