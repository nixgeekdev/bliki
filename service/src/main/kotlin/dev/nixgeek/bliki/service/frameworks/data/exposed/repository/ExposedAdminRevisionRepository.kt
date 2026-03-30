package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Revision
import dev.nixgeek.bliki.service.domain.repository.AdminRevisionRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RevisionTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Exposed ORM implementation of [AdminRevisionRepository] for managing revision records
 * in the admin database.
 *
 * This repository provides administrative operations for revision management, including
 * creating, updating, and deleting revision records using Exposed SQL DSL.
 *
 * @property dbProvider The database provider for transaction management
 * @see AdminRevisionRepository
 * @see RevisionTable
 * @see Revision
 */
@Component
class ExposedAdminRevisionRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRevisionRepository {
    /**
     * Saves or updates a revision record in the admin database.
     *
     * If the revision has no ID, a new record is created. Otherwise, the existing record
     * is updated using an upsert operation.
     *
     * @param revision The revision to save or update
     * @return A [Mono] emitting the saved revision with its generated or existing ID
     */
    override fun save(revision: Revision): Mono<Revision> =
        txMono(DatabaseTarget.ADMIN) {
            RevisionTable
                .upsertReturning(RevisionTable.id) {
                    if (revision.id != null) {
                        it[RevisionTable.id] = revision.id.toString()
                    }
                    it[RevisionTable.entryId] = revision.entryId.toString()
                    it[RevisionTable.authorId] = revision.authorId.toString()
                    it[RevisionTable.diff] = revision.diff
                    it[RevisionTable.summary] = revision.summary
                    it[RevisionTable.event] = revision.event
                    it[RevisionTable.createdAt] = revision.createdAt
                }.single()
                .toRevisionModel()
        }

    /**
     * Deletes a revision record by its ID from the admin database.
     *
     * @param id The ULID of the revision to delete
     * @return A [Mono] emitting the deleted revision, or empty if no revision was found
     */
    override fun delete(id: ULID): Mono<Revision> =
        txMono(DatabaseTarget.ADMIN) {
            RevisionTable
                .deleteReturning { RevisionTable.id eq id.toString() }
                .singleOrNull()
                ?.toRevisionModel()
        }
}
