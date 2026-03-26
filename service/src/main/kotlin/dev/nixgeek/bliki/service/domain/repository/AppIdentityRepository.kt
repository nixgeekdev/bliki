package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.PublicIdentity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppIdentityRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Identity>

    fun fetchById(id: ULID): Mono<Identity>

    fun fetchPublicById(id: ULID): Mono<PublicIdentity> =
        fetchById(id).map { it.toPublicIdentity() }

    fun fetchByEmail(email: String): Mono<Identity>

    fun fetchPublicByEmail(email: String): Mono<PublicIdentity> =
        fetchByEmail(email).map { it.toPublicIdentity() }
}
