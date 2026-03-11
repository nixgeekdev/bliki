package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.PublicProfile
import dev.nixgeek.bliki.service.domain.model.PublicRole
import ulid.ULID

data class PublicProfileResponse(
    val id: ULID,
    val profile: PublicProfile,
    val roles: List<PublicRole>,
)
