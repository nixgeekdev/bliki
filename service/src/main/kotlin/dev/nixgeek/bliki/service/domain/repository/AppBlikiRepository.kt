package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Bliki
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppBlikiRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Bliki>

    fun fetchById(id: ULID): Mono<Bliki>

    fun fetchByAuthorId(authorId: ULID): Flux<Bliki>

    fun fetchByGeneratorId(generatorId: ULID): Flux<Bliki>
}
