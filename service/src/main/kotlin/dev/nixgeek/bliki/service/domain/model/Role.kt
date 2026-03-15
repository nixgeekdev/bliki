package dev.nixgeek.bliki.service.domain.model

import ulid.ULID
import kotlin.time.Instant

data class Role(
    val id: ULID? = null,
    val role: IdentityRole,
    val label: String,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    fun toPublicRole(): PublicRole =
        PublicRole(
            id = id!!,
            role = role,
            label = label,
        )

    fun toSecureRole(): SecureRole =
        SecureRole(
            id = id!!,
            role = role,
        )
}
