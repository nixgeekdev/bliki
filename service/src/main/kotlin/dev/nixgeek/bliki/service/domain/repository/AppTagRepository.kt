package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.model.TagNode
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AppTagRepository : ReactorContextAwareRepository {
    fun fetchAll(): Flux<Tag>

    fun fetchById(id: ULID): Mono<Tag>

    fun fetchChildren(id: ULID): Flux<Tag>

    fun fetchParent(id: ULID): Mono<Tag>

    fun fetchDescendants(rootId: ULID): Flux<Tag>

    fun fetchDescendantTree(rootId: ULID): Mono<TagNode>
}
