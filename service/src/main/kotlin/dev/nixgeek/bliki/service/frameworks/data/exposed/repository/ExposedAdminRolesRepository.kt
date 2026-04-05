package dev.nixgeek.bliki.service.frameworks.data.exposed.repository

import dev.nixgeek.bliki.lib.data.DatabaseProvider
import dev.nixgeek.bliki.lib.data.DatabaseTarget
import dev.nixgeek.bliki.service.domain.model.Identity
import dev.nixgeek.bliki.service.domain.model.Role
import dev.nixgeek.bliki.service.domain.repository.AdminRolesRepository
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityRoleTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.IdentityTable
import dev.nixgeek.bliki.service.frameworks.data.exposed.relation.RoleTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ulid.ULID

/**
 * Exposed-based implementation of [AdminRolesRepository] for administrative role operations.
 *
 * This repository provides role management functionality for the admin database,
 * including role persistence, assignment to identities, and deletion operations.
 * All operations are executed within database transactions targeting the ADMIN database.
 *
 * @property dbProvider The database provider for transaction management
 */
@Component
class ExposedAdminRolesRepository(
    override val dbProvider: DatabaseProvider,
) : AdminRolesRepository {
    /**
     * Saves or updates a role in the admin database.
     *
     * If the role has an ID, it performs an upsert operation. Otherwise, a new role is created.
     * The operation returns the persisted role with all database-generated fields populated.
     *
     * @param role The role to save or update
     * @return A [Mono] emitting the saved role
     */
    override fun save(role: Role): Mono<Role> =
        txMono(DatabaseTarget.ADMIN) {
            RoleTable
                .upsertReturning(RoleTable.id) {
                    if (role.id != null) {
                        it[RoleTable.id] = role.id.toString()
                    }
                    it[RoleTable.role] = role.role.name
                    it[RoleTable.label] = role.label
                    it[RoleTable.createdAt] = role.createdAt
                    it[RoleTable.updatedAt] = role.updatedAt
                }.single()
                .toRoleModel()
        }

    /**
     * Assigns multiple roles to an identity.
     *
     * Creates associations between the specified roles and identity. Duplicate role IDs
     * are automatically filtered out. Returns the updated identity with its role assignments.
     *
     * @param roleIds List of role IDs to assign to the identity
     * @param identityId The ID of the identity receiving the roles
     * @return A [Mono] emitting the updated identity
     */
    override fun assignRolesToIdentity(
        roleIds: List<ULID>,
        identityId: ULID,
    ): Mono<Identity> =
        txMono(DatabaseTarget.ADMIN) {
            val distinctRoleIds = roleIds.distinct()

            IdentityRoleTable.batchInsert(distinctRoleIds) { roleId ->
                this[IdentityRoleTable.identityId] = EntityID(identityId.toString(), IdentityTable)
                this[IdentityRoleTable.roleId] = EntityID(roleId.toString(), RoleTable)
            }

            IdentityTable
                .selectAll()
                .where { IdentityTable.id eq identityId.toString() }
                .single()
                .toIdentityModel()
        }

    /**
     * Removes all roles from an identity.
     *
     * Deletes all role associations for the specified identity, effectively revoking
     * all permissions. Returns the updated identity after all roles have been removed.
     *
     * @param identityId The ID of the identity from which all roles will be removed
     * @return A [Mono] emitting the updated identity with no role assignments
     */
    override fun removeAllRolesFromIdentity(identityId: ULID): Mono<Identity> =
        txMono(DatabaseTarget.ADMIN) {
            IdentityRoleTable.deleteWhere {
                IdentityRoleTable.identityId eq identityId.toString()
            }

            IdentityTable
                .selectAll()
                .where { IdentityTable.id eq identityId.toString() }
                .single()
                .toIdentityModel()
        }

    /**
     * Removes multiple roles from an identity.
     *
     * Deletes the associations between the specified roles and identity. Duplicate role IDs
     * are automatically filtered out. If the role list is empty, no deletion occurs.
     * Returns the updated identity after role removal.
     *
     * @param roleIds List of role IDs to remove from the identity
     * @param identityId The ID of the identity losing the roles
     * @return A [Mono] emitting the updated identity
     */
    override fun removeRolesFromIdentity(
        roleIds: List<ULID>,
        identityId: ULID,
    ): Mono<Identity> =
        txMono(DatabaseTarget.ADMIN) {
            val distinctRoleIds = roleIds.distinct()

            if (distinctRoleIds.isNotEmpty()) {
                IdentityRoleTable.deleteWhere {
                    (IdentityRoleTable.identityId eq identityId.toString()) and
                        (IdentityRoleTable.roleId inList distinctRoleIds.map { it.toString() })
                }
            }

            IdentityTable
                .selectAll()
                .where { IdentityTable.id eq identityId.toString() }
                .single()
                .toIdentityModel()
        }

    /**
     * Deletes a role by its ID.
     *
     * Removes the role from the admin database and returns the deleted role.
     * Returns an empty [Mono] if the role does not exist.
     *
     * @param id The ID of the role to delete
     * @return A [Mono] emitting the deleted role, or empty if not found
     */
    override fun delete(id: ULID): Mono<Role> =
        txMono(DatabaseTarget.ADMIN) {
            RoleTable
                .deleteReturning { RoleTable.id eq id.toString() }
                .singleOrNull()
                ?.toRoleModel()
        }
}
