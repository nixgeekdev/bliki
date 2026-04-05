package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import dev.nixgeek.bliki.service.domain.repository.AdminIdentitySecurityRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Repository implementation for managing identity security operations in the admin database.
 *
 * This repository provides access to user identity and role information stored in the admin database,
 * utilizing Jetbrains Exposed framework for database operations. It handles the retrieval of secure
 * identity credentials and role assignments for authentication and authorization purposes.
 *
 * The repository interacts with three main database tables:
 * - `identity`: Stores user identity information including email and password credentials
 * - `role`: Contains available security roles in the system
 * - `identity_role`: Junction table linking identities to their assigned roles
 *
 * All database operations are executed within transactions using the [DatabaseTarget.ADMIN] target,
 * ensuring proper transaction management and reactive context propagation.
 *
 * ## Usage Examples
 *
 * ### Fetch identity by email:
 * ```kotlin
 * repository.findByEmail("user@example.com")
 *     .subscribe { identity ->
 *         println("Found identity: ${identity.id}")
 *         println("Password hash: ${identity.passwordHash}")
 *     }
 * ```
 *
 * ### Fetch roles for a specific identity:
 * ```kotlin
 * val identityId = ULID.randomULID()
 * repository.findRolesByIdentityId(identityId)
 *     .collectList()
 *     .subscribe { roles ->
 *         roles.forEach { role ->
 *             println("Role: ${role.name}")
 *         }
 *     }
 * ```
 *
 * ### Complete authentication flow:
 * ```kotlin
 * repository.findByEmail("admin@example.com")
 *     .flatMap { identity ->
 *         repository.findRolesByIdentityId(identity.id)
 *             .collectList()
 *             .map { roles -> identity to roles }
 *     }
 *     .subscribe { (identity, roles) ->
 *         println("User ${identity.email} has ${roles.size} roles")
 *     }
 * ```
 *
 * @property dbProvider The database provider used for executing transactions
 * @see AdminIdentitySecurityRepository
 * @see SecureIdentity
 * @see SecureRole
 * @see IdentityTable
 * @see RoleTable
 * @see IdentityRoleTable
 */
@Component
class ExposedAdminIdentitySecurityRepository(
    override val dbProvider: DatabaseProvider,
) : AdminIdentitySecurityRepository {
    /**
     * Retrieves a secure identity by email address from the admin database.
     *
     * This method queries the `identity` table to find a user with the specified email address
     * and returns their secure credentials (ID and password hash). The operation is executed
     * within a reactive transaction context using [DatabaseTarget.ADMIN].
     *
     * @param email The email address to search for (must be unique in the identity table)
     * @return A [Mono] emitting the [SecureIdentity] if found, or an empty Mono if no identity
     *         exists with the specified email address
     */
    override fun fetchByEmail(email: String): Mono<SecureIdentity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityTable
                .selectAll()
                .where { IdentityTable.email eq email }
                .singleOrNull()
                ?.toIdentityModel()
                ?.toSecureIdentity()
        }

    /**
     * Retrieves all security roles assigned to a specific identity.
     *
     * This method performs a join across three tables (`identity`, `identity_role`, and `role`)
     * to fetch all roles associated with the given identity ID. The operation uses inner joins
     * to ensure only valid role assignments are returned, and executes within a reactive
     * transaction context using [DatabaseTarget.ADMIN].
     *
     * The query joins:
     * - `identity` with `identity_role` on identity ID
     * - `identity_role` with `role` on role ID
     *
     * @param identityId The unique identifier of the identity whose roles should be retrieved
     * @return A [Flux] emitting all [SecureRole] objects assigned to the identity, or an empty
     *         Flux if the identity has no roles assigned
     */
    override fun fetchRolesByIdentityId(identityId: ULID): Flux<SecureRole> =
        txFlux(DatabaseTarget.ADMIN) {
            IdentityTable
                .join(IdentityRoleTable, JoinType.INNER, IdentityTable.id, IdentityRoleTable.identityId)
                .join(RoleTable, JoinType.INNER, IdentityRoleTable.roleId, RoleTable.id)
                .selectAll()
                .where { IdentityTable.id eq identityId.toString() }
                .map { it.toRoleModel().toSecureRole() }
        }
}
