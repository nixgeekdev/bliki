package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminRolesRepository : ReactorContextAwareRepository {
    fun save(role: Role): Mono<Role>

    fun assignRolesToIdentity(roleIds: List<ULID>, identityId: ULID): Mono<Identity>

    fun removeAllRolesFromIdentity(identityId: ULID): Mono<Identity>

    fun removeRolesFromIdentity(roleIds: List<ULID>, identityId: ULID): Mono<Identity>

    fun delete(id: ULID): Mono<Role>
}
