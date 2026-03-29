package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.SecureProfile
import dev.nixgeek.bliki.service.domain.repository.AdminProfileRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminProfileRepository(
    override val dbProvider: DatabaseProvider,
) : AdminProfileRepository, AbstractFakeTestRepository<ULID, Profile>() {
    override fun fetchSecureById(id: ULID): Mono<SecureProfile> =
        blockingMono { cache[id]?.toSecureProfile() }

    override fun save(profile: Profile): Mono<Profile> =
        blockingMono {
            val now = Clock.System.now()
            val key = profile.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    identityId = profile.identityId,
                    fullName = profile.fullName,
                    affiliation = profile.affiliation,
                    createdAt = profile.createdAt ?: now,
                    updatedAt = now,
                ) ?: profile.copy(
                    id = key,
                    updatedAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Profile> =
        blockingMono { cache.remove(id) }

    override fun create(record: Profile): Profile {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
