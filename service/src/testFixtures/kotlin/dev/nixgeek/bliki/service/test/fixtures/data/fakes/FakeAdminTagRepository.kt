package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Tag
import dev.nixgeek.bliki.service.domain.repository.AdminTagRepository
import reactor.core.publisher.Mono
import ulid.ULID
import kotlin.time.Clock

class FakeAdminTagRepository(
    override val dbProvider: DatabaseProvider,
) : AdminTagRepository, AbstractFakeTestRepository<ULID, Tag>() {
    override fun save(tag: Tag): Mono<Tag> =
        blockingMono {
            val now = Clock.System.now()
            val key = tag.id ?: ULID.randomULID().toULID()
            val existing = cache[key]

            val saved =
                existing?.copy(
                    parentId = tag.parentId,
                    term = tag.term,
                    label = tag.label,
                    scheme = tag.scheme,
                    createdAt = existing.createdAt ?: now,
                    updatedAt = now,
                ) ?: tag.copy(
                    id = key,
                    updatedAt = now,
                )

            cache[key] = saved
            saved
        }

    override fun delete(id: ULID): Mono<Tag> =
        blockingMono { cache.remove(id) }

    override fun assignParent(id: ULID, parentId: ULID): Mono<Tag> =
        blockingMono {
            val tag = cache[id] ?: throw NoSuchElementException("Tag with id $id not found")
            val now = Clock.System.now()
            val updated = tag.copy(
                parentId = parentId,
                updatedAt = now,
            )
            cache[id] = updated
            updated
        }

    override fun removeParent(id: ULID): Mono<Tag> =
        blockingMono {
            val tag = cache[id] ?: throw NoSuchElementException("Tag with id $id not found")
            val now = Clock.System.now()
            val updated = tag.copy(
                parentId = null,
                updatedAt = now,
            )
            cache[id] = updated
            updated
        }

    override fun create(record: Tag): Tag {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }
}
