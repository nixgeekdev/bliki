package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

/**
 * Junction table representing the many-to-many relationship between identities and roles.
 *
 * This table implements a composite identity pattern where an identity can have multiple roles,
 * and a role can be assigned to multiple identities. The table uses a composite primary key
 * consisting of both the identity ID and role ID to ensure uniqueness of the relationship.
 *
 * This is used primarily by the authentication system to determine what permissions and access
 * levels a given identity has within the admin security context.
 *
 * @see IdentityTable
 * @see RoleTable
 * @see AdminIdentitySecurityRepository
 */
object IdentityRoleTable : CompositeIdTable("identity_roles") {
    /**
     * Foreign key reference to the identity in the [IdentityTable].
     * Part of the composite primary key.
     */
    val identityId = reference("identity_id", IdentityTable.id)

    /**
     * Foreign key reference to the role in the [RoleTable].
     * Part of the composite primary key.
     */
    val roleId = reference("role_id", RoleTable.id)

    /**
     * Composite primary key consisting of both [identityId] and [roleId].
     * This ensures that an identity can only be assigned a specific role once.
     */
    override val primaryKey = PrimaryKey(identityId, roleId)
}
