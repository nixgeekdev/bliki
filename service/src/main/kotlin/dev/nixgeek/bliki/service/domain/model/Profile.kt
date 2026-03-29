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
    fun toPublicProfile(): PublicProfile =
        PublicProfile(
            id = id,
            identityId = identityId,
            fullName = fullName,
        )

    fun toSecureProfile(): SecureProfile =
        SecureProfile(
            id = id!!,
            identityId = identityId,
            fullName = fullName,
            affiliation = affiliation,
        )
}
