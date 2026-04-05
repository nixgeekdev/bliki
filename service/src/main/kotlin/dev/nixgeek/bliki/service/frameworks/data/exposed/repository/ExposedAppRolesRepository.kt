package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AppRolesRepository
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
 * Exposed-based implementation of [AppRolesRepository] for read-only role operations.
 *
 * This repository provides read-only access to role data and identity-role relationships
 * using the APP database target. It supports querying roles by ID, fetching roles assigned
 * to identities, and retrieving identities that have specific roles.
 *
 * @property dbProvider The database provider used for transaction management
 */
@Component
class ExposedAppRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AppRolesRepository {
    /**
     * Fetches all roles from the database.
     *
     * @return A [Flux] emitting all [Role] entities
     */
    override fun fetchAll(): Flux<Role> =
        txFlux(DatabaseTarget.APP) {
            RoleTable
                .selectAll()
                .map { it.toRoleModel() }
        }

    /**
     * Fetches a role by its unique identifier.
     *
     * @param id The unique identifier of the role
     * @return A [Mono] emitting the [Role] if found, or empty if not found
     */
    override fun fetchById(id: ULID): Mono<Role> =
        txMono(DatabaseTarget.APP) {
            RoleTable
                .selectAll()
                .where { RoleTable.id eq id.toString() }
                .singleOrNull()
                ?.toRoleModel()
        }

    /**
     * Fetches all roles assigned to a specific identity.
     *
     * @param identityId The unique identifier of the identity
     * @return A [Flux] emitting all [Role] entities assigned to the identity
     */
    override fun fetchByIdentityId(identityId: ULID): Flux<Role> =
        txFlux(DatabaseTarget.APP) {
            IdentityRoleTable
                .join(RoleTable, JoinType.INNER, IdentityRoleTable.roleId, RoleTable.id)
                .selectAll()
                .where { IdentityRoleTable.identityId eq identityId.toString() }
                .map { it.toRoleModel() }
        }

    /**
     * Fetches all identities that have been assigned a specific role.
     *
     * @param roleId The role id to search for
     * @return A [Flux] emitting all [Identity] entities that have the specified role
     */
    override fun fetchIdentitiesByRoleId(roleId: ULID): Flux<Identity> =
        txFlux(DatabaseTarget.APP) {
            IdentityRoleTable
                .join(IdentityTable, JoinType.INNER, IdentityRoleTable.identityId, IdentityTable.id)
                .selectAll()
                .where { IdentityRoleTable.roleId eq roleId.toString() }
                .map { it.toIdentityModel() }
        }
}
