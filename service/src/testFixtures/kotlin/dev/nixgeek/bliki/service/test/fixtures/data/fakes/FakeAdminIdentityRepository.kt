package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.repository.AdminIdentityRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminIdentityRepository(
    override val dbProvider: DatabaseProvider,
) : AdminIdentityRepository, AbstractFakeTestRepository<ULID, Identity>() {
    override fun fetchSecureById(id: ULID): Mono<SecureIdentity> =
        blockingMono { cache[id]?.toSecureIdentity() }

    override fun fetchSecureByEmail(email: String): Mono<SecureIdentity> =
        blockingMono {
            cache.values.singleOrNull { it.email == email }?.toSecureIdentity()
        }

    override fun save(identity: Identity): Mono<Identity> =
        blockingMono {
            val now = Clock.System.now()
            val key = identity.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    email = identity.email,
                    passwordHash = identity.passwordHash,
                    createdAt = identity.createdAt ?: now,
                    updatedAt = now,
                ) ?: identity.copy(
                    id = key,
                    updatedAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Identity> =
        blockingMono {
            cache.remove(id)
        }

    override fun create(record: Identity): Identity {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
