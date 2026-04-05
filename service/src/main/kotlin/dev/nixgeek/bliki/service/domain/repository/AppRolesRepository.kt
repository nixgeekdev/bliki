package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppRolesRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Role>

    fun fetchById(id: ULID): Mono<Role>

    fun fetchByIdentityId(identityId: ULID): Flux<Role>

    fun fetchIdentitiesByRoleId(roleId: ULID): Flux<Identity>
}
