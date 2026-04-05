package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AdminRolesRepository
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock

class FakeAdminRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRolesRepository, AbstractFakeTestRepository<ULID, Role>() {
    internal val roleIdToIdentityId: ConcurrentHashMap<String, Pair<ULID, ULID>> = ConcurrentHashMap()
    internal val identities: ConcurrentHashMap<ULID, Identity> = ConcurrentHashMap()

    override fun save(role: Role): Mono<Role> =
        blockingMono {
            val now = Clock.System.now()
            val key = role.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    role = role.role,
                    label = role.label,
                    createdAt = role.createdAt ?: now,
                    updatedAt = now,
                ) ?: role.copy(
                    id = key,
                    updatedAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun assignRolesToIdentity(
        roleIds: List<ULID>,
        identityId: ULID,
    ): Mono<Identity> =
        blockingMono {
            roleIds.distinct().forEach { roleId ->
                val key = "$roleId:$identityId"
                roleIdToIdentityId[key] = roleId to identityId
            }
            identities[identityId]
        }

    override fun removeAllRolesFromIdentity(identityId: ULID): Mono<Identity> =
        blockingMono {
            roleIdToIdentityId.keys.removeIf { it.endsWith(":$identityId") }
            identities[identityId]
        }

    override fun removeRolesFromIdentity(
        roleIds: List<ULID>,
        identityId: ULID,
    ): Mono<Identity> =
        blockingMono {
            roleIds.distinct().forEach { roleId ->
                val key = "$roleId:$identityId"
                roleIdToIdentityId.remove(key)
            }
            identities[identityId]
        }

    override fun delete(id: ULID): Mono<Role> =
        blockingMono {
            roleIdToIdentityId.keys.removeIf { it.startsWith("$id:") }
            cache.remove(id)
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
