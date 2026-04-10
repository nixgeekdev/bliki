package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import dev.nixgeek.bliki.service.domain.repository.AdminEntryRepository
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAdminEntryRepository(
    override val dbProvider: DatabaseProvider,
) : AdminEntryRepository, AbstractFakeTestRepository<ULID, Entry>() {
    override fun save(entry: Entry): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ULID): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun addTagsToEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun removeTagsFromEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun addContributorsToEntry(
        entryId: ULID,
        contributorIds: List<ULID>
    ): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun removeContributorsFromEntry(
        entryId: ULID,
        contributorIds: List<ULID>
    ): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun addRelationToEntry(
        entryId: ULID,
        relatedEntryId: ULID,
        type: EntryRelationType
    ): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun removeRelationFromEntry(entryId: ULID, relatedEntryId: ULID): Mono<Entry> {
        TODO("Not yet implemented")
    }

    override fun create(record: Entry): Entry {
        TODO("Not yet implemented")
    }
}
