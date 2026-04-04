package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AdminRolesRepository
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAdminRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRolesRepository, AbstractFakeTestRepository<ULID, Role>() {
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

    override fun create(record: Role): Role {
        TODO("Not yet implemented")
    }
}
