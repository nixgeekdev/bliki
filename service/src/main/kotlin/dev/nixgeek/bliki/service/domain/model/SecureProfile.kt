package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class SecureProfile(
    val id: ULID,
    val identityId: ULID,
    val fullName: String,
    val affiliation: String? = null,
)
