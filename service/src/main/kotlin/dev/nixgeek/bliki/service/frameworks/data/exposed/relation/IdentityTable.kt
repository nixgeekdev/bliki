package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Exposed table definition for the `identity` table in the admin database.
 *
 * This table stores user identity information for authentication and authorization purposes.
 * Each identity represents a unique user account with credentials and associated roles.
 *
 * The table uses ULID as the primary key (inherited from [AbstractULIDTable]) and includes
 * database-level constraints to ensure data integrity:
 * - Email must not be empty (minimum 1 character)
 * - Password hash must follow the bcrypt format (prefixed with `{bcrypt}`)
 *
 * Related tables:
 * - Links to roles via the `identity_role` junction table
 *
 * @see AbstractULIDTable
 * @see IdentityRoleTable
 * @see RoleTable
 */
object IdentityTable : AbstractULIDTable("identity") {
    /**
     * The email address of the identity, used as the username for authentication.
     * Must be at least 1 character long (enforced by database constraint).
     */
    val email = text("email")

    /**
     * The bcrypt-encoded password hash for the identity.
     * Must be prefixed with `{bcrypt}` (enforced by database constraint).
     * The prefix format follows Spring Security's password encoding scheme.
     */
    val passwordHash = text("password_hash")

    /**
     * Timestamp indicating when the identity record was created.
     * Defaults to the current system time when a new record is inserted.
     */
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())

    /**
     * Timestamp indicating when the identity record was last updated.
     * Defaults to the current system time and should be updated on each modification.
     */
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())

    init {
        check("chk_identity_email_not_empty") {
            email.charLength() greaterEq 1
        }

        check("chk_identity_password_hash_format") {
            passwordHash like "{bcrypt}%"
        }
    }
}
