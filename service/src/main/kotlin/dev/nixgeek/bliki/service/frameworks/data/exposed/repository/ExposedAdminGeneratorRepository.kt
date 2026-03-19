package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Generator
import dev.nixgeek.bliki.service.domain.repository.AdminGeneratorRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.GeneratorTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Repository implementation for administrative operations on Generator entities using Exposed framework.
 *
 * This repository provides write operations (save and delete) for generators in the admin database.
 * It uses the Exposed SQL framework for database interactions and operates on the [DatabaseTarget.ADMIN]
 * database target for administrative operations.
 *
 * The repository interacts with the `generator` table as defined in [GeneratorTable] and supports:
 * - Upserting (insert or update) generator records
 * - Deleting generator records by ID
 *
 * All operations are executed within reactive transactions returning [Mono] types for compatibility
 * with Spring WebFlux and reactive programming patterns.
 *
 * ## Database Schema
 *
 * The repository operates on the `generator` table with the following structure:
 * - `id`: Primary key (ULID)
 * - `name`: Generator name
 * - `version`: Generator version
 * - `uri`: Generator URI
 * - `updated_at`: Timestamp of last update
 *
 * ## Usage Examples
 *
 * ### Save a new generator:
 * ```kotlin
 * val generator = Generator(
 *     id = null, // Will be auto-generated
 *     name = "Hugo",
 *     version = "0.120.0",
 *     uri = "https://gohugo.io",
 *     updatedAt = Instant.now()
 * )
 * repository.save(generator)
 *     .subscribe { saved -> println("Saved: ${saved.id}") }
 * ```
 *
 * ### Update an existing generator:
 * ```kotlin
 * val generator = Generator(
 *     id = existingId,
 *     name = "Hugo",
 *     version = "0.121.0",
 *     uri = "https://gohugo.io",
 *     updatedAt = Instant.now()
 * )
 * repository.save(generator)
 *     .subscribe { updated -> println("Updated: ${updated.id}") }
 * ```
 *
 * ### Delete a generator:
 * ```kotlin
 * repository.delete(generatorId)
 *     .subscribe { deleted -> println("Deleted: ${deleted.name}") }
 * ```
 *
 * @property dbProvider The database provider for managing database connections and transactions
 * @see AdminGeneratorRepository
 * @see GeneratorTable
 * @see Generator
 */
@Component
class ExposedAdminGeneratorRepository(
    override val dbProvider: DatabaseProvider,
) : AdminGeneratorRepository {
    /**
     * Saves (inserts or updates) a generator in the admin database.
     *
     * This method performs an upsert operation:
     * - If [generator.id][Generator.id] is null, a new record is inserted with an auto-generated ID
     * - If [generator.id][Generator.id] is provided, the existing record with that ID is updated
     *
     * The operation is executed within a reactive transaction on the [DatabaseTarget.ADMIN] database.
     *
     * @param generator The generator to save. If the ID is null, a new ID will be generated.
     * @return A [Mono] emitting the saved generator with its generated or existing ID
     */
    override fun save(generator: Generator): Mono<Generator> =
        txMono(DatabaseTarget.ADMIN) {
            GeneratorTable
                .upsertReturning(GeneratorTable.id) {
                    if (generator.id != null) {
                        it[GeneratorTable.id] = generator.id.toString()
                    }
                    it[GeneratorTable.name] = generator.name
                    it[GeneratorTable.version] = generator.version
                    it[GeneratorTable.uri] = generator.uri
                    it[GeneratorTable.updatedAt] = generator.updatedAt
                }.single()
                .toGeneratorModel()
        }

    /**
     * Deletes a generator from the admin database by its ID.
     *
     * This method deletes the generator record with the specified ID and returns the deleted
     * generator data. If no generator with the given ID exists, an empty [Mono] is returned.
     *
     * The operation is executed within a reactive transaction on the [DatabaseTarget.ADMIN] database.
     *
     * @param id The unique identifier of the generator to delete
     * @return A [Mono] emitting the deleted generator, or an empty [Mono] if no generator was found
     */
    override fun delete(id: ULID): Mono<Generator> =
        txMono(DatabaseTarget.ADMIN) {
            GeneratorTable
                .deleteReturning { GeneratorTable.id eq id.toString() }
                .singleOrNull()
                ?.toGeneratorModel()
        }
}
