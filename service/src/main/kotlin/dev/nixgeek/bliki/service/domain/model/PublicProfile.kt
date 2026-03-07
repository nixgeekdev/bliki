package dev.nixgeek.bliki.service.domain.model

import ulid.ULID

data class PublicProfile(
    val id: ULID,
    val publicIdentity: PublicIdentity,
    val fullName: String,
    val affiliation: String? = null,
)
