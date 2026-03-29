package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Profile
import dev.nixgeek.bliki.service.domain.model.SecureProfile
import dev.nixgeek.bliki.service.domain.repository.AdminProfileRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.ProfileTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Repository implementation for managing user profile operations in the admin database.
 *
 * This repository provides CRUD operations for user profiles stored in the admin database,
 * utilizing Jetbrains Exposed framework for database operations. It handles the creation,
 * retrieval, updating, and deletion of user profile information including full names and
 * affiliations associated with identities.
 *
 * The repository interacts with the `profile` table which stores user profile information
 * linked to identity records through the `identityId` foreign key relationship.
 *
 * All database operations are executed within transactions using the [DatabaseTarget.ADMIN] target,
 * ensuring proper transaction management and reactive context propagation.
 *
 * ## Usage Examples
 *
 * ### Fetch secure profile by ID:
 * ```kotlin
 * val profileId = ULID.randomULID()
 * repository.fetchSecureById(profileId)
 *     .subscribe { secureProfile ->
 *         println("Found profile: ${secureProfile.id}")
 *         println("Identity ID: ${secureProfile.identityId}")
 *         println("Full name: ${secureProfile.fullName}")
 *     }
 * ```
 *
 * ### Create a new profile:
 * ```kotlin
 * val identityId = ULID.randomULID()
 * val newProfile = Profile(
 *     id = null,
 *     identityId = identityId,
 *     fullName = "John Doe",
 *     affiliation = "Example Corporation",
 *     createdAt = Clock.System.now(),
 *     updatedAt = Clock.System.now()
 * )
 * repository.save(newProfile)
 *     .subscribe { savedProfile ->
 *         println("Created profile with ID: ${savedProfile.id}")
 *     }
 * ```
 *
 * ### Update an existing profile:
 * ```kotlin
 * val existingProfile = profile.copy(
 *     fullName = "Jane Doe",
 *     affiliation = "New Organization",
 *     updatedAt = Clock.System.now()
 * )
 * repository.save(existingProfile)
 *     .subscribe { updatedProfile ->
 *         println("Updated profile: ${updatedProfile.fullName}")
 *     }
 * ```
 *
 * ### Delete a profile:
 * ```kotlin
 * val profileId = ULID.randomULID()
 * repository.delete(profileId)
 *     .subscribe { deletedProfile ->
 *         println("Deleted profile: ${deletedProfile.fullName}")
 *     }
 * ```
 *
 * @property dbProvider The database provider used for executing transactions
 * @see AdminProfileRepository
 * @see Profile
 * @see SecureProfile
 * @see ProfileTable
 */
@Component
class ExposedAdminProfileRepository(
    override val dbProvider: DatabaseProvider,
) : AdminProfileRepository {
    /**
     * Retrieves a secure profile by its unique identifier from the admin database.
     *
     * This method queries the `profile` table to find a profile with the specified ID
     * and returns its secure representation including identity reference. The operation
     * is executed within a reactive transaction context using [DatabaseTarget.ADMIN].
     *
     * @param id The unique identifier of the profile to retrieve
     * @return A [Mono] emitting the [SecureProfile] if found, or an empty Mono if no profile
     *         exists with the specified ID
     */
    override fun fetchSecureById(id: ULID): Mono<SecureProfile> =
        txMono(DatabaseTarget.ADMIN) {
            ProfileTable
                .selectAll()
                .where { ProfileTable.id eq id.toString() }
                .singleOrNull()
                ?.toProfileModel()
                ?.toSecureProfile()
        }

    /**
     * Creates a new profile or updates an existing profile in the admin database.
     *
     * This method performs an upsert operation on the `profile` table. If the profile has
     * an ID, it updates the existing record; otherwise, it creates a new profile with a
     * generated ID. The operation returns the saved profile with all fields populated,
     * and is executed within a reactive transaction context using [DatabaseTarget.ADMIN].
     *
     * @param profile The profile to save or update (if ID is null, a new profile is created)
     * @return A [Mono] emitting the saved [Profile] with its ID and all fields populated
     */
    override fun save(profile: Profile): Mono<Profile> =
        txMono(DatabaseTarget.ADMIN) {
            ProfileTable
                .upsertReturning(ProfileTable.id) {
                    if (profile.id != null) {
                        it[ProfileTable.id] = profile.id.toString()
                    }
                    it[ProfileTable.identityId] = profile.identityId.toString()
                    it[ProfileTable.fullName] = profile.fullName
                    it[ProfileTable.affiliation] = profile.affiliation
                    it[ProfileTable.createdAt] = profile.createdAt
                    it[ProfileTable.updatedAt] = profile.updatedAt
                }.single()
                .toProfileModel()
        }

    /**
     * Deletes a profile from the admin database by its unique identifier.
     *
     * This method removes the profile record with the specified ID from the `profile` table
     * and returns the deleted profile data. The operation is executed within a reactive
     * transaction context using [DatabaseTarget.ADMIN].
     *
     * @param id The unique identifier of the profile to delete
     * @return A [Mono] emitting the deleted [Profile] if found and deleted, or an empty Mono
     *         if no profile exists with the specified ID
     */
    override fun delete(id: ULID): Mono<Profile> =
        txMono(DatabaseTarget.ADMIN) {
            ProfileTable
                .deleteReturning { ProfileTable.id eq id.toString() }
                .singleOrNull()
                ?.toProfileModel()
        }
}
