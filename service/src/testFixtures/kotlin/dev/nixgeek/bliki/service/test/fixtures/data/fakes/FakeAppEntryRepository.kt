package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import dev.nixgeek.bliki.service.domain.repository.AppEntryRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAppEntryRepository(
    override val dbProvider: DatabaseProvider,
) : AppEntryRepository, AbstractFakeTestRepository<ULID, Entry>() {
    override fun fetchAll(): Flux<Entry> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Entry> = blockingMono { cache[id] }

    override fun fetchByBlikiId(blikiId: ULID): Flux<Entry> =
        blockingFlux { cache.values.filter { it.blikiId == blikiId } }

    override fun fetchByTagId(tagId: ULID): Flux<Entry> {
        TODO("Not yet implemented")
    }

    override fun fetchByAuthorId(authorId: ULID): Flux<Entry> {
        TODO("Not yet implemented")
    }

    override fun fetchRelated(
        entryId: ULID,
        type: EntryRelationType?,
        limit: Int
    ): Flux<Entry> {
        TODO("Not yet implemented")
    }

    override fun fetchLatest(limit: Int): Flux<Entry> {
        TODO("Not yet implemented")
    }

    override fun create(record: Entry): Entry {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
