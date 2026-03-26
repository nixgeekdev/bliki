package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Profile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppProfileRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Profile>

    fun fetchById(id: ULID): Mono<Profile>

    fun fetchByIdentityId(identityId: ULID): Mono<Profile>
}
