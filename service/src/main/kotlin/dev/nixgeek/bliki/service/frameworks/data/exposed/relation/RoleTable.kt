package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed ORM table definition for the `roles` table.
 *
 * This table stores role definitions used in the identity and access management system.
 * Roles define permission levels and are associated with identities through the
 * [IdentityRoleTable] junction table to implement role-based access control (RBAC).
 *
 * Common roles include:
 * - ADMIN: Administrative access with full system privileges
 * - AUTHOR: Content creation and management privileges
 * - VIEWER: Read-only access
 *
 * @see IdentityTable for user identity information
 * @see IdentityRoleTable for the many-to-many relationship between identities and roles
 */
object RoleTable : AbstractULIDTable("roles") {
    /**
     * The unique role identifier/name.
     *
     * This is typically an uppercase string constant (e.g., "ADMIN", "AUTHOR")
     * that corresponds to [dev.nixgeek.bliki.service.domain.model.IdentityRole] enum values.
     */
    val role = text("role")

    /**
     * The human-readable label for the role.
     *
     * This provides a user-friendly description of the role that can be displayed
     * in user interfaces (e.g., "Administrator", "Content Author").
     */
    val label = text("label")

    /**
     * The timestamp when this role was created.
     *
     * Defaults to the current system time when a new role is inserted.
     * Nullable to support legacy data or special cases.
     */
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())

    /**
     * The timestamp when this role was last updated.
     *
     * Defaults to the current system time and should be updated whenever
     * the role definition is modified. Nullable to support legacy data or special cases.
     */
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())
}
