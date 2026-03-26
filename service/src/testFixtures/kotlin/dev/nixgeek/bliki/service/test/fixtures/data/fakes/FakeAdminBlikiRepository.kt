package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.repository.AdminBlikiRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminBlikiRepository(
    override val dbProvider: DatabaseProvider,
) : AdminBlikiRepository, AbstractFakeTestRepository<ULID, Bliki>() {
    override fun save(bliki: Bliki): Mono<Bliki> =
        blockingMono {
            val now = Clock.System.now()
            val key = bliki.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    title = bliki.title,
                    subtitle = bliki.subtitle,
                    rights = bliki.rights,
                    baseUri = bliki.baseUri,
                    iconUri = bliki.iconUri,
                    logoUri = bliki.logoUri,
                    lang = bliki.lang,
                    authorId = bliki.authorId,
                    generatorId = bliki.generatorId,
                    updatedAt = bliki.updatedAt ?: now,
                ) ?: bliki.copy(
                    id = key,
                    updatedAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Bliki> =
        blockingMono {
            cache.remove(id)
        }

    override fun create(record: Bliki): Bliki {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
