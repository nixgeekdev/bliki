package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Profile(
    val id: ULID? = null,
    val identityId: ULID,
    val fullName: String,
    val affiliation: String? = null,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    fun toPublicProfile(identity: Identity): PublicProfile =
        PublicProfile(
            id = id!!,
            publicIdentity = identity.toPublicIdentity(),
            fullName = fullName,
            affiliation = affiliation,
        )
}
