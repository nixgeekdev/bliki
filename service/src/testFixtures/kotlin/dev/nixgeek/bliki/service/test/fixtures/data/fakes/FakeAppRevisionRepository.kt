package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.repository.AppRevisionRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

class FakeAppRevisionRepository(
    override val dbProvider: DatabaseProvider,
) : AppRevisionRepository, AbstractFakeTestRepository<ULID, Revision>() {
    private val entryIdToRevisionId: ConcurrentHashMap<ULID, ULID> = ConcurrentHashMap()
    private val authorIdToRevisionId: ConcurrentHashMap<ULID, ULID> = ConcurrentHashMap()

    override fun fetchAll(): Flux<Revision> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Revision> = blockingMono { cache[id] }

    override fun fetchByEntryId(entryId: ULID): Flux<Revision> =
        blockingFlux {
            entryIdToRevisionId
                .keys
                .filter { it == entryId }
                .mapNotNull { cache[entryIdToRevisionId[it]] }
        }

    override fun fetchByAuthorId(authorId: ULID): Flux<Revision> =
        blockingFlux {
            authorIdToRevisionId
                .keys
                .filter { it == authorId }
                .mapNotNull { cache[authorIdToRevisionId[it]] }
        }

    override fun create(record: Revision): Revision {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        entryIdToRevisionId[record.entryId] = key
        authorIdToRevisionId[record.authorId] = key
        return created
    }

    override fun clear() {
        super.clear()
        entryIdToRevisionId.clear()
        authorIdToRevisionId.clear()
    }
}
