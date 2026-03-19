package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AppGeneratorRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Spring component repository implementation for managing Generator entities using Exposed ORM.
 *
 * This repository provides read-only access to generator data stored in the application database.
 * It implements the [AppGeneratorRepository] interface and uses the Exposed framework for database
 * operations against the [GeneratorTable].
 *
 * Generators represent the static site generator tools (like Hugo, Jekyll, etc.) that can be used
 * to build Bliki sites. Each generator has a unique identifier and can be associated with multiple
 * Bliki instances through the Bliki-Generator relationship.
 *
 * ## Database Operations
 *
 * All database operations are executed within reactive transactions using the `txFlux` and `txMono`
 * helpers, which provide proper transaction management and context propagation for reactive streams.
 * Operations target the [DatabaseTarget.APP] database.
 *
 * ## Supported Queries
 *
 * - Fetch all generators from the database
 * - Fetch a specific generator by its unique ULID identifier
 * - Fetch a generator associated with a specific Bliki through a join operation
 *
 * ## Usage Example
 *
 * ```kotlin
 * @Autowired
 * lateinit var generatorRepository: ExposedAppGeneratorRepository
 *
 * // Fetch all generators
 * generatorRepository.fetchAll()
 *     .subscribe { generator -> println(generator.name) }
 *
 * // Fetch specific generator by ID
 * val generatorId = ULID.randomULID()
 * generatorRepository.fetchById(generatorId)
 *     .subscribe { generator -> println(generator) }
 *
 * // Fetch generator for a specific Bliki
 * val blikiId = ULID.randomULID()
 * generatorRepository.fetchByBlikiId(blikiId)
 *     .subscribe { generator -> println(generator) }
 * ```
 *
 * @property dbProvider The database provider used for transaction management and database access
 * @see AppGeneratorRepository
 * @see GeneratorTable
 * @see BlikiTable
 * @see Generator
 */
@Component
class ExposedAppGeneratorRepository(
    override val dbProvider: DatabaseProvider,
) : AppGeneratorRepository {
    /**
     * Fetches all generators from the database.
     *
     * This method retrieves all records from the [GeneratorTable] and maps them to domain model
     * [Generator] objects. The operation is executed within a reactive transaction context.
     *
     * @return A [Flux] emitting all [Generator] instances found in the database
     */
    override fun fetchAll(): Flux<Generator> =
        txFlux(DatabaseTarget.APP) {
            GeneratorTable
                .selectAll()
                .map { it.toGeneratorModel() }
        }

    /**
     * Fetches a specific generator by its unique identifier.
     *
     * This method queries the [GeneratorTable] for a generator matching the provided ULID.
     * If no generator is found with the given ID, the returned [Mono] will be empty.
     *
     * @param id The unique ULID identifier of the generator to fetch
     * @return A [Mono] emitting the [Generator] if found, or an empty [Mono] if not found
     */
    override fun fetchById(id: ULID): Mono<Generator> =
        txMono(DatabaseTarget.APP) {
            GeneratorTable
                .selectAll()
                .where { GeneratorTable.id eq id.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }

    /**
     * Fetches the generator associated with a specific Bliki.
     *
     * This method performs an INNER JOIN between [GeneratorTable] and [BlikiTable] to retrieve
     * the generator that is configured for the specified Bliki instance. The join is performed
     * on the generator ID foreign key relationship.
     *
     * If no Bliki is found with the given ID, or if the Bliki doesn't have an associated generator,
     * the returned [Mono] will be empty.
     *
     * @param blikiId The unique ULID identifier of the Bliki whose generator should be fetched
     * @return A [Mono] emitting the associated [Generator] if found, or an empty [Mono] if not found
     */
    override fun fetchByBlikiId(blikiId: ULID): Mono<Generator> =
        txMono(DatabaseTarget.APP) {
            GeneratorTable
                .join(BlikiTable, JoinType.INNER, GeneratorTable.id, BlikiTable.generatorId)
                .selectAll()
                .where { BlikiTable.id eq blikiId.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }
}
