package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Bliki
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminBlikiRepository : ReactorContextAwareRepository {
    fun save(bliki: Bliki): Mono<Bliki>

    fun delete(id: ULID): Mono<Bliki>
}
