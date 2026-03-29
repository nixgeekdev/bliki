package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.repository.AdminRevisionRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminRevisionRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRevisionRepository, AbstractFakeTestRepository<ULID, Revision>() {
    override fun save(revision: Revision): Mono<Revision> =
        blockingMono {
            val now = Clock.System.now()
            val key = revision.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    entryId = revision.entryId,
                    authorId = revision.authorId,
                    diff = revision.diff,
                    summary = revision.summary,
                    event = revision.event,
                    createdAt = revision.createdAt,
                ) ?: revision.copy(
                    id = key,
                    createdAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Revision> =
        blockingMono { cache.remove(id) }

    override fun create(record: Revision): Revision {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
