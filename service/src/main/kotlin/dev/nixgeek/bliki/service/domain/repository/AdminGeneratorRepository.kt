package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Generator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppGeneratorRepository : ReactorContextAwareRepository {
    suspend fun fetchAll(): Flux<Generator>

    suspend fun fetchById(id: ULID): Mono<Generator>

    suspend fun fetchByBlikiId(blikiId: ULID): Mono<Generator>
}

interface AdminGeneratorRepository : ReactorContextAwareRepository {
    suspend fun save(generator: Generator): Mono<Generator>

    suspend fun delete(id: ULID): Mono<Generator>
}
