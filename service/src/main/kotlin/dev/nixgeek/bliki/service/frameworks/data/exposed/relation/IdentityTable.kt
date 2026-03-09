package dev.nixgeek.bliki.service.frameworks.data.exposed.relation

import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object IdentityTable : AbstractULIDTable("identity") {
    val email = text("email")
    val passwordHash = text("password_hash")
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    init {
        check("chk_identity_email_not_empty") {
            email.charLength() greaterEq 1
        }

        check("chk_identity_password_hash_format") {
            passwordHash like $$"$argon2%"
        }
    }
}
