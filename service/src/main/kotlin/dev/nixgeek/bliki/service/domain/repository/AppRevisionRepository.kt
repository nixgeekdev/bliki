package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Revision
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppRevisionRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Revision>

    fun fetchById(id: ULID): Mono<Revision>

    fun fetchByEntryId(entryId: ULID): Flux<Revision>

    fun fetchByAuthorId(authorId: ULID): Flux<Revision>
}
