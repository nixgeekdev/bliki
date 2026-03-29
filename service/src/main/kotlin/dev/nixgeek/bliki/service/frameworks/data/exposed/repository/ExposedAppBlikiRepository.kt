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
 * This repository provides reactive database operations for Bliki domain objects using the
 * Exposed SQL framework. All database operations are executed within transactions targeting
 * the APP database and return reactive types (Mono/Flux) for integration with Spring WebFlux.
 *
 * @property dbProvider The database provider for managing database connections and transactions
 * @see AppBlikiRepository
 * @see BlikiTable
 * @see Bliki
 */
@Component
class ExposedAppBlikiRepository(
    override val dbProvider: DatabaseProvider,
) : AppBlikiRepository {

    /**
     * Fetches all Bliki entities from the database.
     *
     * @return A [Flux] emitting all Bliki entities in the database
     */
    override fun fetchAll(): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .map { it.toBlikiModel() }
        }

    /**
     * Fetches a single Bliki entity by its unique identifier.
     *
     * @param id The unique identifier of the Bliki to fetch
     * @return A [Mono] emitting the Bliki if found, or empty if not found
     */
    override fun fetchById(id: ULID): Mono<Bliki> =
        txMono(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.id eq id.toString() }
                .singleOrNull()
                ?.toBlikiModel()
        }

    /**
     * Fetches all Bliki entities created by a specific author.
     *
     * @param authorId The unique identifier of the author
     * @return A [Flux] emitting all Bliki entities created by the specified author
     */
    override fun fetchByAuthorId(authorId: ULID): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.authorId eq authorId.toString() }
                .map { it.toBlikiModel() }
        }

    /**
     * Fetches all Bliki entities associated with a specific generator.
     *
     * @param generatorId The unique identifier of the generator
     * @return A [Flux] emitting all Bliki entities associated with the specified generator
     */
    override fun fetchByGeneratorId(generatorId: ULID): Flux<Bliki> =
        txFlux(DatabaseTarget.APP) {
            BlikiTable
                .selectAll()
                .where { BlikiTable.generatorId eq generatorId.toString() }
                .map { it.toBlikiModel() }
        }
}
