package dev.nixgeek.bliki.service.domain.model.response

import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.model.PublicIdentity
import ulid.ULID

data class GeneratorResponse(
    val id: ULID,
    val generator: Generator,
    val identity: PublicIdentity,
)
