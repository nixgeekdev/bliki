package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.repository.AppRevisionRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Exposed-based implementation of [AppRevisionRepository].
 *
 * Provides reactive database operations for managing revision records using the Exposed ORM framework.
 * All operations are executed within transactional contexts against the APP database target.
 *
 * @property dbProvider The database provider for managing database connections and transactions
 * @see AppRevisionRepository
 * @see RevisionTable
 * @see Revision
 */
@Component
class ExposedAppRevisionRepository(
    override val dbProvider: DatabaseProvider,
) : AppRevisionRepository {
    /**
     * Retrieves all revisions from the database.
     *
     * @return A [Flux] emitting all [Revision] records
     */
    override fun fetchAll(): Flux<Revision> =
        txFlux(DatabaseTarget.APP) {
            RevisionTable
                .selectAll()
                .map { it.toRevisionModel() }
        }

    /**
     * Retrieves a single revision by its unique identifier.
     *
     * @param id The unique identifier of the revision
     * @return A [Mono] emitting the matching [Revision], or empty if not found
     */
    override fun fetchById(id: ULID): Mono<Revision> =
        txMono(DatabaseTarget.APP) {
            RevisionTable
                .selectAll()
                .where { RevisionTable.id eq id.toString() }
                .singleOrNull()
                ?.toRevisionModel()
        }

    /**
     * Retrieves all revisions associated with a specific entry.
     *
     * @param entryId The unique identifier of the entry
     * @return A [Flux] emitting all [Revision] records for the specified entry
     */
    override fun fetchByEntryId(entryId: ULID): Flux<Revision> =
        txFlux(DatabaseTarget.APP) {
            RevisionTable
                .selectAll()
                .where { RevisionTable.entryId eq entryId.toString() }
                .map { it.toRevisionModel() }
        }

    /**
     * Retrieves all revisions created by a specific author.
     *
     * @param authorId The unique identifier of the author
     * @return A [Flux] emitting all [Revision] records created by the specified author
     */
    override fun fetchByAuthorId(authorId: ULID): Flux<Revision> =
        txFlux(DatabaseTarget.APP) {
            RevisionTable
                .selectAll()
                .where { RevisionTable.authorId eq authorId.toString() }
                .map { it.toRevisionModel() }
        }
}
