package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Bliki
import dev.nixgeek.bliki.service.domain.repository.AdminBlikiRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.BlikiTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID


/**
 * Repository implementation for managing Bliki entities in the admin database using Exposed ORM framework.
 *
 * This repository provides administrative operations for Bliki entities, including save and delete operations.
 * All database operations are executed within transactions targeting the ADMIN database.
 *
 * @property dbProvider The database provider used to establish database connections and manage transactions.
 *
 * @see AdminBlikiRepository
 * @see Bliki
 */
@Component
class ExposedAdminBlikiRepository(
    override val dbProvider: DatabaseProvider,
) : AdminBlikiRepository {
    /**
     * Saves or updates a Bliki entity in the admin database.
     *
     * If the Bliki has an ID, it will be updated (upsert operation). Otherwise, a new ID will be generated.
     * The operation is executed within a transaction targeting the ADMIN database.
     *
     * @param bliki The Bliki entity to save or update.
     * @return A Mono emitting the saved Bliki entity with all fields populated.
     */
    override fun save(bliki: Bliki): Mono<Bliki> =
        txMono(DatabaseTarget.ADMIN) {
            BlikiTable
                .upsertReturning(BlikiTable.id) {
                    if (bliki.id != null) {
                        it[BlikiTable.id] = bliki.id.toString()
                    }
                    it[BlikiTable.title] = bliki.title
                    it[BlikiTable.subtitle] = bliki.subtitle
                    it[BlikiTable.rights] = bliki.rights
                    it[BlikiTable.baseUri] = bliki.baseUri
                    it[BlikiTable.iconUri] = bliki.iconUri
                    it[BlikiTable.logoUri] = bliki.logoUri
                    it[BlikiTable.lang] = bliki.lang
                    it[BlikiTable.authorId] = bliki.authorId.toString()
                    it[BlikiTable.generatorId] = bliki.generatorId.toString()
                    it[BlikiTable.updatedAt] = bliki.updatedAt
                }.single()
                .toBlikiModel()
        }

    /**
     * Deletes a Bliki entity from the admin database by its ID.
     *
     * The operation is executed within a transaction targeting the ADMIN database.
     *
     * @param id The ULID of the Bliki entity to delete.
     * @return A Mono emitting the deleted Bliki entity if found, or an empty Mono if no entity exists with the given ID.
     */
    override fun delete(id: ULID): Mono<Bliki> =
        txMono(DatabaseTarget.ADMIN) {
            BlikiTable
                .deleteReturning { BlikiTable.id eq id.toString() }
                .singleOrNull()
                ?.toBlikiModel()
        }
}
