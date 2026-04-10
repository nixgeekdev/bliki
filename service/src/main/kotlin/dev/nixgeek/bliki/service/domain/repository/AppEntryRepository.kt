package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Entry
import dev.nixgeek.bliki.service.domain.model.EntryRelationType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppEntryRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Entry>

    fun fetchById(id: ULID): Mono<Entry>

    fun fetchByBlikiId(blikiId: ULID): Flux<Entry>

    fun fetchByTagId(tagId: ULID): Flux<Entry>

    fun fetchByAuthorId(authorId: ULID): Flux<Entry>

    fun fetchRelated(entryId: ULID, type: EntryRelationType? = null, limit: Int = 10): Flux<Entry>

    fun fetchLatest(limit: Int = 10): Flux<Entry>
}
