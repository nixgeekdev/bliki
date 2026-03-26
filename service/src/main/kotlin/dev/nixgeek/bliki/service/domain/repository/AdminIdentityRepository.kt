package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminIdentityRepository : ReactorContextAwareRepository {
    fun save(identity: Identity): Mono<Identity>

    fun delete(id: ULID): Mono<Identity>
}
