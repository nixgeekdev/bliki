package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.model.TagNode
import dev.nixgeek.bliki.service.domain.repository.AppTagRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAppTagRepository(
    override val dbProvider: DatabaseProvider
) : AppTagRepository, AbstractFakeTestRepository<ULID, Tag>() {
    override fun fetchAll(): Flux<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchById(id: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchChildren(id: ULID): Flux<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchParent(parentId: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchDescendants(rootId: ULID): Flux<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchDescendantTree(rootId: ULID): Mono<TagNode> {
        TODO("Not yet implemented")
    }

    override fun create(record: Tag): Tag {
        TODO("Not yet implemented")
    }
}
