package dev.nixgeek.bliki.service.test.fixtures.data.fakes

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.ulid.toULID
import dev.nixgeek.bliki.lib.test.fixtures.data.fakes.AbstractFakeTestRepository
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AppGeneratorRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

class FakeAppGeneratorRepository(
    override val dbProvider: DatabaseProvider,
) : AppGeneratorRepository, AbstractFakeTestRepository<ULID, Generator>() {
    private val blikiToGeneratorId: ConcurrentHashMap<ULID, ULID> = ConcurrentHashMap()

    // If order matters in tests, either sort or use matchers that ignore order
    override fun fetchAll(): Flux<Generator> = blockingFlux { cache.values }

    override fun fetchById(id: ULID): Mono<Generator> = blockingMono { cache[id] }

    override fun fetchByBlikiId(blikiId: ULID): Mono<Generator> =
        blockingMono { cache[blikiToGeneratorId[blikiId]] }

    override fun create(record: Generator): Generator {
        val key = record.id ?: ULID.randomULID().toULID()
        val created = record.copy(id = record.id ?: key)
        cache[key] = created
        return created
    }

    fun create(bliki: Bliki): Bliki {
        val key = bliki.id ?: ULID.randomULID().toULID()
        val created = bliki.copy(id = bliki.id ?: key)
        blikiToGeneratorId[key] = created.generatorId
        return created
    }

    override fun clear() {
        super.clear()
        blikiToGeneratorId.clear()
    }
}
