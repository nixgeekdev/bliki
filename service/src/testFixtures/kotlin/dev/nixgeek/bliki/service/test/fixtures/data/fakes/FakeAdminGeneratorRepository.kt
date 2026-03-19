package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminGeneratorRepository(
    override val dbProvider: DatabaseProvider,
) : AdminGeneratorRepository, AbstractFakeTestRepository<ULID, Generator>() {
    override fun save(generator: Generator): Mono<Generator> =
        blockingMono {
            val now = Clock.System.now()
            val key = generator.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    name = generator.name,
                    version = generator.version,
                    uri = generator.uri,
                    updatedAt = generator.updatedAt ?: now,
                ) ?: generator.copy(
                    id = key,
                    createdAt = generator.createdAt ?: now,
                    updatedAt = generator.updatedAt ?: now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Generator> =
        blockingMono {
            cache.remove(id)
        }

    override fun create(record: Generator): Generator {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
