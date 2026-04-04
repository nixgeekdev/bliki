package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AdminRolesRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

@Component
class ExposedAdminRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRolesRepository {
    override fun save(role: Role): Mono<Role> {
        TODO("Not yet implemented")
    }

    override fun assignRolesToIdentity(
        roleIds: List<ULID>,
        identityId: ULID
    ): Mono<Identity> {
        TODO("Not yet implemented")
    }

    override fun removeRolesFromIdentity(
        roleIds: List<ULID>,
        identityId: ULID
    ): Mono<Identity> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ULID): Mono<Role> {
        TODO("Not yet implemented")
    }
}
