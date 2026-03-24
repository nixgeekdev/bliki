package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.repository.AppBlikiRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID


/**
 * Exposed-based implementation of [AppBlikiRepository] for managing Bliki entities.
 *
 * This repository provides reactive database operations for Bliki entities using the Exposed SQL framework.
 * All database operations are executed within transactions targeting the APP database and return reactive types
 * (Mono/Flux) for non-blocking data access.
 *
 * ## Usage
 * ```kotlin
 * // Fetch all blikis
 * repository.fetchAll().collectList().block()
 *
 * // Fetch by ID
 * repository.fetchById(ULID.randomULID()).block()
 *
 * // Fetch by author
 * repository.fetchByAuthorId(authorId).collectList().block()
 *
 * // Fetch by generator
 * repository.fetchByGeneratorId(generatorId).block()
 * ```
 *
 * @property dbProvider The database provider used for transaction management
 * @see AppBlikiRepository
 * @see BlikiTable
 */
@Component
class ExposedAppBlikiRepository(
    override val dbProvider: DatabaseProvider,
) : AppBlikiRepository {
    override fun fetchAll(): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .map { it.toBlikiModel() }
        }

    override fun fetchById(id: ULID): Mono<Bliki> =
        txMono(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.id eq id.toString() }
                .singleOrNull()
                ?.toBlikiModel()
        }

    override fun fetchByAuthorId(authorId: ULID): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.authorId eq authorId.toString() }
                .map { it.toBlikiModel() }
        }

    override fun fetchByGeneratorId(generatorId: ULID): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.generatorId eq generatorId.toString() }
                .map { it.toBlikiModel() }
        }
}
