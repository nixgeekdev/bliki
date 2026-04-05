package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AppRolesRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

class FakeAppRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AppRolesRepository, AbstractFakeTestRepository<ULID, Role>() {
    internal val roleIdToIdentityId: ConcurrentHashMap<String, Pair<ULID, ULID>> = ConcurrentHashMap()
    internal val identities: ConcurrentHashMap<ULID, Identity> = ConcurrentHashMap()

    override fun fetchAll(): Flux<Role> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Role> = blockingMono { cache[id] }

    override fun fetchByIdentityId(identityId: ULID): Flux<Role> =
        blockingFlux {
            roleIdToIdentityId
                .values
                .filter { it.second == identityId }
                .mapNotNull { cache[it.first] }
        }

    override fun fetchIdentitiesByRoleId(roleId: ULID): Flux<Identity> =
        blockingFlux {
            roleIdToIdentityId.values
                .filter { it.first == roleId }
                .mapNotNull { identities[it.second] }
        }

    override fun create(record: Role): Role {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }

    fun create(identity: Identity): Identity {
        val key = identity.id ?: ULID.randomULID().toULID()
        val created = identity.copy(id = identity.id ?: key)
        identities[key] = created
        return created
    }

    fun create(roleId: ULID, identityId: ULID) {
        val key = "$roleId:$identityId"
        roleIdToIdentityId[key] = roleId to identityId
    }

    override fun clear() {
        super.clear()
        roleIdToIdentityId.clear()
        identities.clear()
    }
}
