package dev.nixgeek.bliki.service.domain.repository

import dev.nixgeek.bliki.lib.data.ReactorContextAwareRepository
import dev.nixgeek.bliki.service.domain.model.SecureIdentity
import dev.nixgeek.bliki.service.domain.model.SecureRole
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ulid.ULID

interface AdminIdentitySecurityRepository : ReactorContextAwareRepository {
    fun findByEmail(email: String): Mono<SecureIdentity>

    fun findRolesByIdentityId(identityId: ULID): Flux<SecureRole>
}
