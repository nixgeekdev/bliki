package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.PublicProfile
import ulid.ULID

data class BlikiResponse(
    val id: ULID,
    val bliki: Bliki,
    val author: PublicProfile,
    val generator: GeneratorResponse,
)
