package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import ulid.ULID

interface IdentitySecurityRepository : ContextAwareRepository {
    fun findByEmail(email: String): SecureIdentity?

    fun findRolesByIdentityId(identityId: ULID): List<SecureRole>
}
