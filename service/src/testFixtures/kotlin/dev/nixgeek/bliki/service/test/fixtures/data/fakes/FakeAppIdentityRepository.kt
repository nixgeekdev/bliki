package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.repository.AppIdentityRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAppIdentityRepository(
    override val dbProvider: DatabaseProvider,
) : AppIdentityRepository, AbstractFakeTestRepository<ULID, Identity>() {
    override fun fetchAll(): Flux<Identity> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Identity> = blockingMono { cache[id] }

    override fun fetchByEmail(email: String): Mono<Identity> =
        blockingMono { cache.values.singleOrNull { it.email == email } }

    override fun create(record: Identity): Identity {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
