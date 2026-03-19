package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

/**
 * Represents a user profile associated with an identity.
 *
 * Profiles contain personal information such as full name and affiliation for users in the system.
 * Each profile is uniquely linked to an identity via a one-to-one relationship.
 *
 * ## Table Structure
 * - **Primary Key**: `id` (ULID, inherited from [AbstractULIDTable])
 * - **Columns**:
 *   - `identity_id`: Foreign key reference to [IdentityTable], with RESTRICT on delete
 *   - `full_name`: User's full name (TEXT, NOT NULL, minimum 1 character per schema constraint)
 *   - `affiliation`: User's organizational affiliation (TEXT, nullable)
 *   - `created_at`: Timestamp when the profile was created (defaults to current time)
 *   - `updated_at`: Timestamp when the profile was last updated (defaults to current time)
 *
 * ## Database Constraints
 * - Identity ID must reference a valid identity record (foreign key with RESTRICT on delete)
 * - Full name must not be empty (CHECK constraint: `LENGTH(full_name) >= 1`)
 * - Identity ID should be unique (one profile per identity)
 *
 * ## Related Tables
 * - [IdentityTable]: Parent table linked via `identity_id` foreign key
 *
 * @see IdentityTable
 * @see AbstractULIDTable
 */
object ProfileTable : AbstractULIDTable("profile") {
    val identityId =
        reference(
            name = "identity_id",
            refColumn = IdentityTable.id,
            onDelete = ReferenceOption.RESTRICT,
        )
    val fullName = text("full_name")
    val affiliation = text("affiliation").nullable()
    val createdAt = timestamp("created_at").nullable().default(Clock.System.now())
    val updatedAt = timestamp("updated_at").nullable().default(Clock.System.now())
}
