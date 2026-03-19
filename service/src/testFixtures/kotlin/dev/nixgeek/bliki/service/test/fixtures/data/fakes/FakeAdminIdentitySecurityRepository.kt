package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import dev.nixgeek.bliki.service.domain.repository.AdminIdentitySecurityRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

class FakeAdminIdentitySecurityRepository(
    override val dbProvider: DatabaseProvider,
) : AdminIdentitySecurityRepository, AbstractFakeTestRepository<ULID, SecureIdentity>() {
    internal val identityIdToRoleId: ConcurrentHashMap<String, Pair<ULID, ULID>> = ConcurrentHashMap()
    internal val roles: ConcurrentHashMap<ULID, SecureRole> = ConcurrentHashMap()

    override fun findByEmail(email: String): Mono<SecureIdentity> =
        blockingMono { cache.values.firstOrNull { it.email == email } }

    override fun findRolesByIdentityId(identityId: ULID): Flux<SecureRole> =
        blockingFlux {
            roles.values.filter { role ->
                role.id in
                    identityIdToRoleId.entries
                        .filter { it.value.first == identityId }
                        .map { it.value.second }
            }
        }

    override fun create(record: SecureIdentity): SecureIdentity {
        require(record.passwordHash.contains("{bcrypt}")) {
            "Password hash must be bcrypt"
        }
        cache[record.id] = record
        return record
    }

    fun create(identityId: ULID, roleId: ULID) {
        val key = "$identityId:$roleId"
        identityIdToRoleId[key] = identityId to roleId
    }

    fun create(role: SecureRole): SecureRole {
        roles[role.id] = role
        return role
    }

    override fun clear() {
        super.clear()
        identityIdToRoleId.clear()
        roles.clear()
    }
}
