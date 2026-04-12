package dev.nixgeek.bliki.service.domain.model.response

import ulid.ULID

data class PublicProfileIdentityResponse(
    val identityId: ULID,
    val profileId: ULID,
    val fullName: String,
    val email: String,
    val affiliation: String? = null,
)
