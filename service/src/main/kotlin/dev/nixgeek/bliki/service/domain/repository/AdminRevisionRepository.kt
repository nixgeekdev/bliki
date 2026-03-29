package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Revision
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminRevisionRepository : ReactorContextAwareRepository {
    fun save(revision: Revision): Mono<Revision>

    fun delete(id: ULID): Mono<Revision>
}
