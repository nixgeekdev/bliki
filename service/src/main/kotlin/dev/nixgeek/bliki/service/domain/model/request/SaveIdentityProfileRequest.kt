package dev.nixgeek.bliki.service.domain.model.request

import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Profile
import ulid.ULID
import kotlin.time.Clock

data class SaveIdentityProfileRequest(
    val identityId: String? = null,
    val profileId: String? = null,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val affiliation: String? = null,
) {
    val isIdentityUpdatable: Boolean = identityId != null

    val isProfileUpdatable: Boolean = profileId != null

    fun toIdentity(identity: ULID? = null): Identity =
        Identity(
            id = identityId?.toULID() ?: identity,
            email = email,
            passwordHash = passwordHash,
            createdAt = null,
            updatedAt = Clock.System.now(),
        )

    fun toProfile(profile: ULID? = null, identity: ULID? = null): Profile =
        Profile(
            id = profileId?.toULID() ?: profile,
            identityId = identityId?.toULID() ?: identity!!,
            fullName = fullName,
            affiliation = affiliation,
            createdAt = null,
            updatedAt = Clock.System.now(),
        )
}
