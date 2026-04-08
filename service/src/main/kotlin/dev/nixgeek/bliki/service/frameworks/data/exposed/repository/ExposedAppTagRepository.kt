package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.repository.AppTagRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

@Component
class ExposedAppTagRepository(
    override val dbProvider: DatabaseProvider,
) : AppTagRepository {
    override fun fetchAll(): Flux<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchById(id: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchChildren(id: ULID): Flux<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchParent(id: ULID): Mono<Tag> {
        TODO("Not yet implemented")
    }

    override fun fetchDescendants(rootId: ULID): Flux<Tag> {
        TODO("Not yet implemented")
    }
}
