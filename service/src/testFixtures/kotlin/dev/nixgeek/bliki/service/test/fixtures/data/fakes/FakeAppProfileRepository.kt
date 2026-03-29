package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.PublicProfile
import dev.nixgeek.bliki.service.domain.repository.AppProfileRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAppProfileRepository(
    override val dbProvider: DatabaseProvider,
) : AppProfileRepository, AbstractFakeTestRepository<ULID, Profile>() {
    override fun fetchAll(): Flux<Profile> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Profile> = blockingMono { cache[id] }

    override fun fetchPublicById(id: ULID): Mono<PublicProfile> =
        blockingMono { cache[id]?.toPublicProfile() }

    override fun fetchByIdentityId(identityId: ULID): Mono<Profile> =
        blockingMono { cache.values.singleOrNull { it.identityId == identityId } }

    override fun create(record: Profile): Profile {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
