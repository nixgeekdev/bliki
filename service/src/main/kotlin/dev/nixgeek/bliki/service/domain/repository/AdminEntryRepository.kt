package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminEntryRepository : ReactorContextAwareRepository {
    fun save(entry: Entry): Mono<Entry>

    fun delete(id: ULID): Mono<Entry>

    fun addTagsToEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry>

    fun removeTagsFromEntry(entryId: ULID, tagIds: List<ULID>): Mono<Entry>

    fun addContributorsToEntry(entryId: ULID, contributorIds: List<ULID>): Mono<Entry>

    fun removeContributorsFromEntry(entryId: ULID, contributorIds: List<ULID>): Mono<Entry>

    fun addRelationToEntry(entryId: ULID, relatedEntryId: ULID, type: EntryRelationType): Mono<Entry>

    fun removeRelationFromEntry(entryId: ULID, relatedEntryId: ULID): Mono<Entry>
}
