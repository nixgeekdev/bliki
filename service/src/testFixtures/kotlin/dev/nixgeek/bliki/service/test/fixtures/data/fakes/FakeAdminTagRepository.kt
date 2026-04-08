package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.repository.AdminTagRepository
import reactor.core.publisher.Mono
import ulid.ULID

class FakeAdminTagRepository(
    override val dbProvider: DatabaseProvider,
) : AdminTagRepository, AbstractFakeTestRepository<ULID, Tag>() {
    override fun save(tag: Tag): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun delete(id: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun assignParent(id: ULID, parentId: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun removeParent(id: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun create(record: Tag): Tag {
        TODO("Not yet implemented")
    }
}
