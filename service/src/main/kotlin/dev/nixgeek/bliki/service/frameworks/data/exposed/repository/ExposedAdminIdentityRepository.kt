package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.repository.AdminIdentityRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Exposed-based implementation of [AdminIdentityRepository] for managing identity persistence
 * in the admin database.
 *
 * This repository provides CRUD operations for identity records using Exposed ORM with reactive
 * Mono wrappers for database transactions.
 *
 * @property dbProvider The database provider used to access the admin database
 */
@Component
class ExposedAdminIdentityRepository(
    override val dbProvider: DatabaseProvider,
) : AdminIdentityRepository {
    /**
     * Fetches a secure identity record by its unique identifier.
     *
     * Returns the complete identity including the password hash, which should only be used
     * internally for authentication purposes. The password hash must never be exposed in
     * public APIs.
     *
     * @param id The ULID of the identity to retrieve
     * @return A Mono emitting the secure identity with password hash, or empty if no identity was found
     */
    override fun fetchSecureById(id: ULID): Mono<SecureIdentity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.id eq id.toString() }
                .singleOrNull()
                ?.toIdentityModel()
                ?.toSecureIdentity()
        }

    /**
     * Fetches a secure identity record by email address.
     *
     * Returns the complete identity including the password hash, which should only be used
     * internally for authentication purposes. The password hash must never be exposed in
     * public APIs.
     *
     * @param email The email address of the identity to retrieve
     * @return A Mono emitting the secure identity with password hash, or empty if no identity was found
     */
    override fun fetchSecureByEmail(email: String): Mono<SecureIdentity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.email eq email }
                .singleOrNull()
                ?.toIdentityModel()
                ?.toSecureIdentity()
        }

    /**
     * Saves or updates an identity record in the database.
     *
     * If the identity has an ID, it will update the existing record; otherwise, it will insert
     * a new record with a generated ID.
     *
     * @param identity The identity to save or update
     * @return A Mono emitting the saved identity with all fields populated from the database
     */
    override fun save(identity: Identity): Mono<Identity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityTable
                .upsertReturning(IdentityTable.id) {
                    if (identity.id != null) {
                        it[IdentityTable.id] = identity.id.toString()
                    }
                    it[IdentityTable.email] = identity.email
                    it[IdentityTable.passwordHash] = identity.passwordHash
                    it[IdentityTable.createdAt] = identity.createdAt
                    it[IdentityTable.updatedAt] = identity.updatedAt
                }.single()
                .toIdentityModel()
        }

    /**
     * Deletes an identity record by its ID and returns the deleted record.
     *
     * @param id The ULID of the identity to delete
     * @return A Mono emitting the deleted identity, or null if no identity was found with the given ID
     */
    override fun delete(id: ULID): Mono<Identity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityTable
                .deleteReturning { IdentityTable.id eq id.toString() }
                .singleOrNull()
                ?.toIdentityModel()
        }
}
