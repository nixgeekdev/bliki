package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Generator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppGeneratorRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Generator>

    fun fetchById(id: ULID): Mono<Generator>

    fun fetchByBlikiId(blikiId: ULID): Mono<Generator>
}
