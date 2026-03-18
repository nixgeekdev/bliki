package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Exposed table mapping for the `identity_public` database view.
 *
 * This view provides a public-facing representation of identity data, exposing only
 * non-sensitive information. It excludes security-critical fields such as password hashes
 * that are available in the full [IdentityTable].
 *
 * The view is used for scenarios where identity information needs to be displayed or
 * queried without exposing sensitive authentication credentials.
 *
 * @property email The email address associated with the identity (unique identifier for users)
 * @property createdAt Timestamp indicating when the identity was originally created
 * @property updatedAt Timestamp indicating when the identity was last modified
 *
 * @see IdentityTable for the full identity table with security fields
 */
object IdentityPublicView : AbstractULIDTable("identity_public") {
    val email = text("email")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
