package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.PublicRole
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureProfile
import ulid.ULID

data class SecureIdentityResponse(
    val id: ULID,
    val identity: SecureIdentity,
    val profile: SecureProfile,
    val roles: List<PublicRole>,
)
