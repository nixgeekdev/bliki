package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

/**
 * Internal identity model. The password hash value should never, ever
 * be exposed on the wire (except from the client on creation and to and
 * from the database). Hence, the public and the secure identity models.
 */
data class Identity(
    val id: ULID? = null,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    fun toPublicIdentity(): PublicIdentity =
        PublicIdentity(
            id = id!!,
            email = email,
        )

    fun toSecureIdentity(): SecureIdentity =
        SecureIdentity(
            id = id!!,
            email = email,
            passwordHash = passwordHash,
        )
}
