package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.model.TagNode
import dev.nixgeek.bliki.service.domain.repository.AppTagRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAppTagRepository(
    override val dbProvider: DatabaseProvider,
) : AppTagRepository, AbstractFakeTestRepository<ULID, Tag>() {
    override fun fetchAll(): Flux<Tag> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Tag> = blockingMono { cache[id] }

    override fun fetchChildren(id: ULID): Flux<Tag> =
        blockingFlux { cache.values.filter { it.parentId == id } }

    override fun fetchParent(id: ULID): Mono<Tag> =
        blockingMono { cache.values.find { it.id == id }?.parentId?.let { cache[it] } }

    override fun fetchDescendants(rootId: ULID): Flux<Tag> =
        blockingFlux { TODO("Not yet implemented") }

    override fun fetchDescendantTree(rootId: ULID): Mono<TagNode> =
        blockingMono { TODO("Not yet implemented") }

    override fun create(record: Tag): Tag {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
