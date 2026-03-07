package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.PublicRole
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import ulid.ULID

data class SecureIdentityResponse(
    val id: ULID,
    val identity: SecureIdentity,
    val profile: Profile,
    val roles: List<PublicRole>,
)
