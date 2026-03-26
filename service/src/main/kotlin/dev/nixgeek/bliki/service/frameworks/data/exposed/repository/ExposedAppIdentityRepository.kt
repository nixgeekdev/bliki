package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.repository.AppIdentityRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Exposed ORM implementation of [AppIdentityRepository] for managing identity data.
 *
 * This Spring component provides reactive data access operations for [Identity] entities
 * using Jetbrains Exposed ORM framework. All database operations are executed within
 * transactional contexts targeting the APP database.
 *
 * @property dbProvider The database provider for obtaining database connections
 */
@Component
class ExposedAppIdentityRepository(
    override val dbProvider: DatabaseProvider,
) : AppIdentityRepository {
    /**
     * Fetches all identities from the database.
     *
     * @return A [Flux] of [Identity] containing all identities in the system
     */
    override fun fetchAll(): Flux<Identity> =
        txFlux(DatabaseTarget.APP) {
            IdentityTable
                .selectAll()
                .map { it.toIdentityModel() }
        }

    /**
     * Fetches a single identity by its unique identifier.
     *
     * @param id The ULID of the identity to retrieve
     * @return A [Mono] of [Identity] if found, or an empty Mono if no identity exists with the given ID
     */
    override fun fetchById(id: ULID): Mono<Identity> =
        txMono(DatabaseTarget.APP) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.id eq id.toString() }
                .singleOrNull()
                ?.toIdentityModel()
        }

    /**
     * Fetches a single identity by email address.
     *
     * @param email The email address of the identity to retrieve
     * @return A [Mono] of [Identity] if found, or an empty Mono if no identity exists with the given email
     */
    override fun fetchByEmail(email: String): Mono<Identity> =
        txMono(DatabaseTarget.APP) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.email eq email }
                .singleOrNull()
                ?.toIdentityModel()
        }
}
