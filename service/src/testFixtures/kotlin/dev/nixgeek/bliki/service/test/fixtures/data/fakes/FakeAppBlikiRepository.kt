package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.repository.AppBlikiRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

class FakeAppBlikiRepository(
    override val dbProvider: DatabaseProvider,
) : AppBlikiRepository, AbstractFakeTestRepository<ULID, Bliki>() {
    private val generatorIdToBlikiId: ConcurrentHashMap<ULID, ULID> = ConcurrentHashMap()
    private val authorIdToBlikiId: ConcurrentHashMap<ULID, ULID> = ConcurrentHashMap()

    override fun fetchAll(): Flux<Bliki> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Bliki> = blockingMono { cache[id] }

    override fun fetchByAuthorId(authorId: ULID): Flux<Bliki> =
        blockingFlux {
            authorIdToBlikiId
                .keys
                .filter { it == authorId }
                .mapNotNull { cache[authorIdToBlikiId[it]] }
        }

    override fun fetchByGeneratorId(generatorId: ULID): Flux<Bliki> =
        blockingFlux {
            generatorIdToBlikiId
                .keys
                .filter { it == generatorId }
                .mapNotNull { cache[generatorIdToBlikiId[it]] }
        }

    override fun create(record: Bliki): Bliki {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        generatorIdToBlikiId[created.generatorId] = created.id!!
        authorIdToBlikiId[created.authorId] = created.id
        return created
    }

    override fun clear() {
        super.clear()
        generatorIdToBlikiId.clear()
        authorIdToBlikiId.clear()
    }
}
