package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.repository.AdminTagRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

@Component
class ExposedAdminTagRepository(
    override val dbProvider: DatabaseProvider,
) : AdminTagRepository {
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
}
