package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Generator
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminGeneratorRepository : ReactorContextAwareRepository {
    fun save(generator: Generator): Mono<Generator>

    fun delete(id: ULID): Mono<Generator>
}
