package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.repository.AppProfileRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Repository implementation for managing user profile operations in the application database.
 *
 * This repository provides access to user profile information stored in the app database,
 * utilizing Jetbrains Exposed framework for database operations. It handles the retrieval of
 * user profile data that extends beyond basic authentication credentials, such as display names,
 * biographical information, and other user-specific settings.
 *
 * The repository interacts with the `profile` table, which stores extended user information
 * linked to identities in the authentication system via the identity ID foreign key.
 *
 * All database operations are executed within transactions using the [DatabaseTarget.APP] target,
 * ensuring proper transaction management and reactive context propagation.
 *
 * ## Usage Examples
 *
 * ### Fetch all profiles:
 * ```kotlin
 * repository.fetchAll()
 *     .collectList()
 *     .subscribe { profiles ->
 *         profiles.forEach { profile ->
 *             println("Profile: ${profile.id}")
 *         }
 *     }
 * ```
 *
 * ### Fetch profile by ID:
 * ```kotlin
 * val profileId = ULID.randomULID()
 * repository.fetchById(profileId)
 *     .subscribe { profile ->
 *         println("Found profile: ${profile.id}")
 *     }
 * ```
 *
 * ### Fetch profile by identity ID:
 * ```kotlin
 * val identityId = ULID.randomULID()
 * repository.fetchByIdentityId(identityId)
 *     .subscribe { profile ->
 *         println("Profile for identity ${identityId}: ${profile.id}")
 *     }
 * ```
 *
 * @property dbProvider The database provider used for executing transactions
 * @see AppProfileRepository
 * @see Profile
 * @see ProfileTable
 */
@Component
class ExposedAppProfileRepository(
    override val dbProvider: DatabaseProvider,
) : AppProfileRepository {
    /**
     * Retrieves all user profiles from the application database.
     *
     * This method queries the `profile` table to fetch all available user profiles.
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     *
     * @return A [Flux] emitting all [Profile] objects in the database, or an empty Flux if no
     *         profiles exist
     */
    override fun fetchAll(): Flux<Profile> =
        txFlux(DatabaseTarget.APP) {
            ProfileTable
                .selectAll()
                .map { it.toProfileModel() }
        }

    /**
     * Retrieves a user profile by its unique identifier.
     *
     * This method queries the `profile` table to find a profile with the specified ID.
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     *
     * @param id The unique identifier of the profile to retrieve
     * @return A [Mono] emitting the [Profile] if found, or an empty Mono if no profile
     *         exists with the specified ID
     */
    override fun fetchById(id: ULID): Mono<Profile> =
        txMono(DatabaseTarget.APP) {
            ProfileTable
                .selectAll()
                .where { ProfileTable.id eq id.toString() }
                .singleOrNull()
                ?.toProfileModel()
        }

    /**
     * Retrieves a user profile by its associated identity identifier.
     *
     * This method queries the `profile` table to find a profile linked to the specified identity ID.
     * The operation is executed within a reactive transaction context using [DatabaseTarget.APP].
     * This is useful for retrieving profile information when only the authentication identity
     * is known, creating a bridge between the authentication and profile data.
     *
     * @param identityId The unique identifier of the identity whose profile should be retrieved
     * @return A [Mono] emitting the [Profile] if found, or an empty Mono if no profile
     *         exists for the specified identity ID
     */
    override fun fetchByIdentityId(identityId: ULID): Mono<Profile> =
        txMono(DatabaseTarget.APP) {
            ProfileTable
                .selectAll()
                .where { ProfileTable.identityId eq identityId.toString() }
                .singleOrNull()
                ?.toProfileModel()
        }
}
