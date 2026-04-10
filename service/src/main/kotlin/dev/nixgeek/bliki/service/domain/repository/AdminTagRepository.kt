package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminTagRepository : ReactorContextAwareRepository {
    fun save(tag: Tag): Mono<Tag>

    fun delete(id: ULID): Mono<Tag>

    fun assignParent(id: ULID, parentId: ULID): Mono<Tag>

    fun removeParent(id: ULID): Mono<Tag>
}
